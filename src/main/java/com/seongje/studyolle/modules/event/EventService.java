package com.seongje.studyolle.modules.event;

import com.seongje.studyolle.domain.account.Account;
import com.seongje.studyolle.domain.event.Enrollment;
import com.seongje.studyolle.domain.event.Event;
import com.seongje.studyolle.domain.event.EventType;
import com.seongje.studyolle.domain.study.Study;
import com.seongje.studyolle.modules.account.repository.AccountRepository;
import com.seongje.studyolle.modules.event.form.EventForm;
import com.seongje.studyolle.modules.event.repository.EnrollmentRepository;
import com.seongje.studyolle.modules.event.repository.EventRepository;
import com.seongje.studyolle.modules.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.seongje.studyolle.domain.event.EventType.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AccountRepository accountRepository;
    private final StudyRepository studyRepository;
    private final ModelMapper modelMapper;

    public Event findEvent(Long id) {
        return eventRepository.searchEventById(id);
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

        Enrollment newEnrollment = Enrollment.createNewEnrollment(findEvent, findAccount);

        if (findEvent.getCreatedBy().equals(findAccount)) {
            newEnrollment.setApproved(true);
        }

        findEvent.addEnrollment(enrollmentRepository.save(newEnrollment));
    }

    @Transactional
    public void updateEvent(Event event, EventForm eventForm) {
        Event findEvent = eventRepository.searchEventById(event.getId());

        modelMapper.map(eventForm, findEvent);

        if ((findEvent.getEventType() == FCFS) && (findEvent.getNumberOfRemainingSeat() > 0)) {
            findEvent.updateEnrollmentsWaitingToApproved();
        }
    }
}
