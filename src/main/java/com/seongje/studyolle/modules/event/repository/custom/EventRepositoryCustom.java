package com.seongje.studyolle.modules.event.repository.custom;

import com.seongje.studyolle.domain.event.Event;

public interface EventRepositoryCustom {

    Event searchEventById(Long id);
}
