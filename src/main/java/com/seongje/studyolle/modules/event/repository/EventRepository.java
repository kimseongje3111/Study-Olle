package com.seongje.studyolle.modules.event.repository;

import com.seongje.studyolle.domain.event.Event;
import com.seongje.studyolle.modules.event.repository.custom.EventRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryCustom {
}