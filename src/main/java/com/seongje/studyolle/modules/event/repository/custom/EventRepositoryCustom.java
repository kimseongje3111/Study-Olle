package com.seongje.studyolle.modules.event.repository.custom;

import com.seongje.studyolle.modules.event.domain.Event;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface EventRepositoryCustom {

    Event searchEventById(Long eventId);

    List<Event> searchEventsByStudy(Long studyId);
}
