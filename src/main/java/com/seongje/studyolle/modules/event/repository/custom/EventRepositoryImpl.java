package com.seongje.studyolle.modules.event.repository.custom;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seongje.studyolle.domain.event.Event;

import javax.persistence.EntityManager;

import static com.seongje.studyolle.domain.account.QAccount.account;
import static com.seongje.studyolle.domain.event.QEvent.*;
import static com.seongje.studyolle.domain.study.QStudy.study;

public class EventRepositoryImpl implements EventRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public EventRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Event searchEventById(Long searchId) {
        return queryFactory
                .selectFrom(event)
                .join(event.createdBy, account).fetchJoin()
                .join(event.study, study).fetchJoin()
                .where(event.id.eq(searchId))
                .fetchOne();
    }
}
