package com.seongje.studyolle.modules.event.service;

import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.event.domain.Enrollment;
import com.seongje.studyolle.modules.event.domain.Event;
import com.seongje.studyolle.modules.study.domain.Study;
import com.seongje.studyolle.modules.account.repository.AccountRepository;
import com.seongje.studyolle.modules.event.form.EventForm;
import com.seongje.studyolle.modules.event.repository.EnrollmentRepository;
import com.seongje.studyolle.modules.event.repository.EventRepository;
import com.seongje.studyolle.modules.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.seongje.studyolle.modules.event.domain.EventType.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AccountRepository accountRepository;
    private final StudyRepository studyRepository;
    private final ModelMapper modelMapper;

    public Event findEvent(Long eventId) {
        Event findEvent = eventRepository.searchEventById(eventId);

        if (findEvent == null) {
            throw new IllegalArgumentException("요청한 '" + eventId + "'에 해당하는 모임이 없습니다.");
        }

        return findEvent;
    }

    public Enrollment findEnrollment(Long enrollmentId) {
        Enrollment findEnrollment = enrollmentRepository.searchEnrollmentById(enrollmentId);

        if (findEnrollment == null) {
            throw new IllegalArgumentException("요청한 '" + enrollmentId + "'에 해당하는 참가 신청자가 없습니다.");
        }

        return findEnrollment;
    }

    @Transactional
    public Event createNewEvent(Event event, Account account, Study study) {
        Account findAccount = accountRepository.findByEmail(account.getEmail());
        Study findStudy = studyRepository.findByPath(study.getPath());

        return eventRepository.save(event.newEvent(findAccount, findStudy));
    }

    @Transactional
    public void enrollToEvent(Event event, Account account) {
        Event findEvent = eventRepository.searchEventById(event.getId());
        Account findAccount = accountRepository.findByEmail(account.getEmail());

        if (enrollmentRepository.existsByEventAndAccount(findEvent, findAccount)) {
            throw new IllegalArgumentException("이미 해당 모임 참가 신청을 완료했습니다.");
        }

        if (!findEvent.isAfterEnrollmentDeadline()) {
            Enrollment newEnrollment = Enrollment.createNewEnrollment(findEvent, findAccount);

            if (findEvent.getCreatedBy().equals(findAccount) || findEvent.isRemainingSeatsFor(FCFS)) {
                newEnrollment.setApproved(true);
            }

            findEvent.addEnrollment(enrollmentRepository.save(newEnrollment));
        }
    }

    @Transactional
    public void cancelEnrollFromEvent(Event event, Account account) {
        Event findEvent = eventRepository.searchEventById(event.getId());
        Account findAccount = accountRepository.findByEmail(account.getEmail());

        if (!enrollmentRepository.existsByEventAndAccount(findEvent, findAccount)) {
            throw new IllegalArgumentException("해당 모임에 참가 신청한 기록이 없습니다.");
        }

        Enrollment findEnrollment =
                enrollmentRepository.searchEnrollmentByEventAndAccount(findEvent.getId(), findAccount.getId());

        if (!findEvent.isAfterEnrollmentDeadline() && !findEnrollment.isAttended()) {
            enrollmentRepository.delete(findEnrollment);
            findEvent.removeEnrollment(findEnrollment);

            if (findEvent.isRemainingSeatsFor(FCFS)) {
                findEvent.updateEnrollmentsWaitingToApproved();
            }
        }
    }

    @Transactional
    public void updateEvent(Event event, EventForm eventForm) {
        Event findEvent = eventRepository.searchEventById(event.getId());

        if (eventForm.getLimitOfEnrollments() >= findEvent.getNumberOfAcceptedEnrollments()) {
            modelMapper.map(eventForm, findEvent);

            if (findEvent.isRemainingSeatsFor(FCFS)) {
                findEvent.updateEnrollmentsWaitingToApproved();
            }
        }
    }

    @Transactional
    public void deleteEvent(Event event) {
        eventRepository.delete(event);
    }

    @Transactional
    public void enrollmentAccept(Event event, Enrollment enrollment) {
        Event findEvent = eventRepository.searchEventById(event.getId());
        Enrollment findEnrollment = enrollmentRepository.searchEnrollmentById(enrollment.getId());

        if (findEvent.canAccept(findEnrollment)) {
            findEnrollment.setApproved(true);
        }
    }

    @Transactional
    public void enrollmentReject(Event event, Enrollment enrollment) {
        Event findEvent = eventRepository.searchEventById(event.getId());
        Enrollment findEnrollment = enrollmentRepository.searchEnrollmentById(enrollment.getId());

        if (findEvent.canReject(findEnrollment)) {
            findEnrollment.setApproved(false);
        }
    }

    @Transactional
    public void enrollmentCheckIn(Event event, Enrollment enrollment) {
        Enrollment findEnrollment = enrollmentRepository.searchEnrollmentById(enrollment.getId());

        if (event.isStarted() && enrollment.isApproved()) {
            findEnrollment.setAttended(true);
        }
    }

    @Transactional
    public void enrollmentCheckInCancel(Event event, Enrollment enrollment) {
        Enrollment findEnrollment = enrollmentRepository.searchEnrollmentById(enrollment.getId());

        if (event.isStarted() && enrollment.isApproved()) {
            findEnrollment.setAttended(false);
        }
    }

    public List<Event> getEventsFor(Long studyId, String state) {
        List<Event> eventList = eventRepository.searchEventsByStudy(studyId);

        if (state.equals("NEW")) {
            return eventList.stream()
                    .filter(event -> !event.isAfterEnrollmentDeadline())
                    .sorted(Comparator.comparing(Event::getEnrollmentDeadline))
                    .collect(Collectors.toList());

        } else if(state.equals("NOW")) {
            return eventList.stream()
                    .filter(event -> event.isAfterEnrollmentDeadline() && !event.isClosed())
                    .sorted(Comparator.comparing(Event::getStartDateTime))
                    .collect(Collectors.toList());
        } else {
            return eventList.stream()
                    .filter(Event::isClosed)
                    .sorted(Comparator.comparing(Event::getEndDateTime))
                    .collect(Collectors.toList());
        }
    }
}
