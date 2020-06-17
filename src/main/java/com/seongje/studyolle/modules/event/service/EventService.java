package com.seongje.studyolle.modules.event.service;

import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.event.domain.Enrollment;
import com.seongje.studyolle.modules.event.domain.Event;
import com.seongje.studyolle.modules.study.domain.Study;
import com.seongje.studyolle.modules.event.form.EventForm;
import com.seongje.studyolle.modules.event.repository.EnrollmentRepository;
import com.seongje.studyolle.modules.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.seongje.studyolle.modules.event.domain.Enrollment.*;
import static com.seongje.studyolle.modules.event.domain.EventType.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

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
        Event newEvent = eventRepository.save(event.newEvent(account, study));

        // TODO : 모임 생성 이벤트

        return newEvent;
    }

    @Transactional
    public void updateEvent(Event event, EventForm eventForm) {
        if (eventForm.getLimitOfEnrollments() >= event.getNumberOfAcceptedEnrollments()) {
            modelMapper.map(eventForm, event);

            if (event.isRemainingSeatsFor(FCFS)) {
                event.updateEnrollmentsWaitingToApproved();
            }

            // TODO : 모임 수정 이벤트
        }
    }

    @Transactional
    public void deleteEvent(Event event) {
        eventRepository.delete(event);

        // TODO : 모임 삭제 이벤트
    }

    @Transactional
    public void enrollToEvent(Event event, Account account) {
        if (enrollmentRepository.existsByEventAndAccount(event, account)) {
            throw new IllegalArgumentException("이미 해당 모임 참가 신청을 완료했습니다.");
        }

        if (!event.isAfterEnrollmentDeadline()) {
            Enrollment newEnrollment = createNewEnrollment(event, account);

            if (event.getCreatedBy().equals(account) || event.isRemainingSeatsFor(FCFS)) {
                newEnrollment.setApproved(true);
            }

            event.addEnrollment(enrollmentRepository.save(newEnrollment));

            // TODO : 모임 신청 이벤트
        }
    }

    @Transactional
    public void cancelEnrollFromEvent(Event event, Account account) {
        if (!enrollmentRepository.existsByEventAndAccount(event, account)) {
            throw new IllegalArgumentException("해당 모임에 참가 신청한 기록이 없습니다.");
        }

        Enrollment findEnrollment =
                enrollmentRepository.searchEnrollmentByEventAndAccount(event.getId(), account.getId());

        if (!event.isAfterEnrollmentDeadline() && !findEnrollment.isAttended()) {
            enrollmentRepository.delete(findEnrollment);
            event.removeEnrollment(findEnrollment);

            if (event.isRemainingSeatsFor(FCFS)) {
                event.updateEnrollmentsWaitingToApproved();
            }

            // TODO : 모임 신청 취소 이벤트
        }
    }

    @Transactional
    public void enrollmentAccept(Event event, Enrollment enrollment) {
        if (event.canAccept(enrollment)) {
            enrollment.setApproved(true);

            // TODO : 모임 신청 결과 이벤트 (관리자 확인)
        }
    }

    @Transactional
    public void enrollmentReject(Event event, Enrollment enrollment) {
        if (event.canReject(enrollment)) {
            enrollment.setApproved(false);

            // TODO : 모임 신청 결과 이벤트 (관리자 확인)
        }
    }

    @Transactional
    public void enrollmentCheckIn(Event event, Enrollment enrollment) {
        if (event.isStarted() && enrollment.isApproved()) {
            enrollment.setAttended(true);

            // TODO : 모임 출석 결과 이벤트
        }
    }

    @Transactional
    public void enrollmentCheckInCancel(Event event, Enrollment enrollment) {
        if (event.isStarted() && enrollment.isApproved()) {
            enrollment.setAttended(false);

            // TODO : 모임 출석 결과 이벤트
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
