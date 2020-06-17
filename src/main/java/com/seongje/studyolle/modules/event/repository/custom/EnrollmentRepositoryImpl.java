package com.seongje.studyolle.modules.event.repository.custom;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seongje.studyolle.modules.event.domain.Enrollment;

import javax.persistence.EntityManager;

import static com.seongje.studyolle.modules.account.domain.QAccount.*;
import static com.seongje.studyolle.modules.event.domain.QEnrollment.enrollment;
import static com.seongje.studyolle.modules.event.domain.QEvent.*;

public class EnrollmentRepositoryImpl implements EnrollmentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public EnrollmentRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Enrollment searchEnrollmentById(Long enrollmentId) {
        return queryFactory
                .selectFrom(enrollment)
                .join(enrollment.event, event).fetchJoin()
                .join(enrollment.account, account).fetchJoin()
                .where(enrollment.id.eq(enrollmentId))
                .fetchOne();
    }

    @Override
    public Enrollment searchEnrollmentByEventAndAccount(Long eventId, Long accountId) {
        return queryFactory
                .selectFrom(enrollment)
                .join(enrollment.event, event).fetchJoin()
                .join(enrollment.account, account).fetchJoin()
                .where(
                        event.id.eq(eventId),
                        account.id.eq(accountId)
                )
                .fetchOne();
    }
}
