package com.seongje.studyolle.modules.event;

import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.event.domain.Enrollment;
import com.seongje.studyolle.modules.event.domain.Event;
import com.seongje.studyolle.modules.event.domain.EventType;
import com.seongje.studyolle.modules.event.repository.EventRepository;
import com.seongje.studyolle.modules.study.domain.Study;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class EventFactory {

    @Autowired EventRepository eventRepository;

    @Transactional
    public Event createEvent(String title, EventType type, int limit, Study study, Account account) {
        Event event = new Event();
        event.setTitle(title);
        event.setEventType(type);
        event.setLimitOfEnrollments(limit);
        event.setStudy(study);
        event.setCreatedBy(account);

        return eventRepository.save(event);
    }
}
