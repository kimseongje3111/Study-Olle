package com.seongje.studyolle.modules.event.repository.custom;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seongje.studyolle.modules.event.domain.Event;

import javax.persistence.EntityManager;

import java.util.List;

import static com.seongje.studyolle.domain.account.QAccount.account;
import static com.seongje.studyolle.domain.event.QEvent.*;
import static com.seongje.studyolle.domain.study.QStudy.study;

public class EventRepositoryImpl implements EventRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public EventRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Event searchEventById(Long eventId) {
        return queryFactory
                .selectFrom(event)
                .join(event.createdBy, account).fetchJoin()
                .join(event.study, study).fetchJoin()
                .where(event.id.eq(eventId))
                .fetchOne();
    }

    @Override
    public List<Event> searchEventsByStudy(Long studyId) {
        return queryFactory
                .selectFrom(event)
                .join(event.createdBy, account).fetchJoin()
                .join(event.study, study).fetchJoin()
                .where(study.id.eq(studyId))
                .fetch();
    }
}
