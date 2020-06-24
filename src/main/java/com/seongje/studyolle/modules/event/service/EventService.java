package com.seongje.studyolle.modules.event.service;

import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.event.app_event.custom.enrollment.EnrollmentAppliedEvent;
import com.seongje.studyolle.modules.event.app_event.custom.enrollment.EnrollmentCancelledEvent;
import com.seongje.studyolle.modules.event.app_event.custom.event.EventCancelledEvent;
import com.seongje.studyolle.modules.event.app_event.custom.event.EventCreatedEvent;
import com.seongje.studyolle.modules.event.app_event.custom.event.EventUpdatedEvent;
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

import java.util.List;

import static com.seongje.studyolle.modules.event.app_event.custom.enrollment.EnrollmentResultType.*;
import static com.seongje.studyolle.modules.event.domain.Enrollment.*;
import static com.seongje.studyolle.modules.event.domain.EventType.*;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

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

        eventPublisher.publishEvent(new EventCreatedEvent(newEvent));

        return newEvent;
    }

    @Transactional
    public void updateEvent(Event event, EventForm eventForm) {
        if (eventForm.getLimitOfEnrollments() >= event.getNumberOfApprovedEnrollments()) {
            modelMapper.map(eventForm, event);

            if (event.isRemainingSeatsFor(FCFS)) {
                event.getWaitingEnrollmentsOfRemainingSeats().forEach(enrollment -> {
                    enrollment.setApproved(true);

                    eventPublisher.publishEvent(new EnrollmentAppliedEvent(enrollment, APPROVED));
                });
            }

            eventPublisher.publishEvent(new EventUpdatedEvent(event));
        }
    }

    @Transactional
    public void deleteEvent(Event event) {
        eventRepository.delete(event);

        eventPublisher.publishEvent(new EventCancelledEvent(event));
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

            if (newEnrollment.isApproved()) {
                eventPublisher.publishEvent(new EnrollmentAppliedEvent(newEnrollment, APPROVED));

            } else {
                eventPublisher.publishEvent(new EnrollmentAppliedEvent(newEnrollment, WAITING));
            }
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
                event.getWaitingEnrollmentsOfRemainingSeats().forEach(enrollment -> {
                    enrollment.setApproved(true);

                    eventPublisher.publishEvent(new EnrollmentAppliedEvent(enrollment, APPROVED));
                });
            }

            eventPublisher.publishEvent(new EnrollmentCancelledEvent(findEnrollment));
        }
    }

    @Transactional
    public void enrollmentAccept(Event event, Enrollment enrollment) {
        if (event.canAccept(enrollment)) {
            enrollment.setApproved(true);

            eventPublisher.publishEvent(new EnrollmentAppliedEvent(enrollment, APPROVED));
        }
    }

    @Transactional
    public void enrollmentReject(Event event, Enrollment enrollment) {
        if (event.canReject(enrollment)) {
            enrollment.setApproved(false);

            eventPublisher.publishEvent(new EnrollmentAppliedEvent(enrollment, REJECTED));
        }
    }

    @Transactional
    public void enrollmentCheckIn(Event event, Enrollment enrollment) {
        if (event.isStarted() && enrollment.isApproved()) {
            enrollment.setAttended(true);
        }
    }

    @Transactional
    public void enrollmentCheckInCancel(Event event, Enrollment enrollment) {
        if (event.isStarted() && enrollment.isApproved()) {
            enrollment.setAttended(false);
        }
    }

    public List<Event> getEventsFor(Long studyId, String state) {
        List<Event> eventList = eventRepository.searchEventsByStudy(studyId);

        if (state.equals("NEW")) {
            return eventList.stream()
                    .filter(event -> !event.isAfterEnrollmentDeadline())
                    .sorted(comparing(Event::getEnrollmentDeadline))
                    .collect(toList());

        } else if(state.equals("NOW")) {
            return eventList.stream()
                    .filter(event -> event.isAfterEnrollmentDeadline() && !event.isClosed())
                    .sorted(comparing(Event::getStartDateTime))
                    .collect(toList());
        } else {
            return eventList.stream()
                    .filter(Event::isClosed)
                    .sorted(comparing(Event::getEndDateTime))
                    .collect(toList());
        }
    }

    public List<Event> getEventsToAttend(Account account) {
        List<Enrollment> findEnrollments =
                enrollmentRepository.searchAllByAccountAndApprovedAndAttended(account.getId(), true, false);

        return findEnrollments.stream()
                .map(Enrollment::getEvent)
                .filter(event -> !event.isClosed())
                .sorted(comparing(Event::getStartDateTime))
                .collect(toList());
    }

    public List<Event> getEventsForAttended(Account account) {
        List<Enrollment> findEnrollments =
                enrollmentRepository.searchAllByAccountAndApprovedAndAttended(account.getId(), true, true);

        return findEnrollments.stream()
                .map(Enrollment::getEvent)
                .filter(Event::isClosed)
                .sorted(comparing(Event::getEndDateTime).reversed())
                .limit(3)
                .collect(toList());
    }
}
