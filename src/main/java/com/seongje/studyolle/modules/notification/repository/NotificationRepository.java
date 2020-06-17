package com.seongje.studyolle.modules.notification.repository;

import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.notification.domain.Notification;
import com.seongje.studyolle.modules.notification.repository.custom.NotificationRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {

    long countByAccountAndChecked(Account account, boolean checked);

    @Transactional
    void deleteByAccountAndChecked(Account account, boolean checked);
}
