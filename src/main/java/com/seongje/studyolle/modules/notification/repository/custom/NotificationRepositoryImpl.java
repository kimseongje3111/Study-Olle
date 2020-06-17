package com.seongje.studyolle.modules.notification.repository.custom;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seongje.studyolle.modules.notification.domain.Notification;

import javax.persistence.EntityManager;
import java.util.List;

import static com.seongje.studyolle.modules.account.domain.QAccount.*;
import static com.seongje.studyolle.modules.notification.domain.QNotification.*;

public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public NotificationRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Notification> searchAllByAccountAndCheckedAndCreatedDateTimeDesc(Long accountId, boolean checked) {
        return queryFactory
                .selectFrom(notification)
                .join(notification.account, account).fetchJoin()
                .where(
                        account.id.eq(accountId),
                        notification.checked.eq(checked)
                )
                .orderBy(notification.createdDateTime.desc())
                .fetch();
    }

    @Override
    public void markAllCheckedAsTrueByAccount(Long accountId) {
        queryFactory
                .update(notification)
                .set(notification.checked, true)
                .where(
                        notification.account.id.eq(accountId),
                        notification.checked.isFalse()
                )
                .execute();
    }
}
