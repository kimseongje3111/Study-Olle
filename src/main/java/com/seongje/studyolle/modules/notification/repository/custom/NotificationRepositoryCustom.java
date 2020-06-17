package com.seongje.studyolle.modules.notification.repository.custom;

import com.seongje.studyolle.modules.notification.domain.Notification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface NotificationRepositoryCustom {

    List<Notification> searchAllByAccountAndCheckedAndCreatedDateTimeDesc(Long accountId, boolean checked);

    @Transactional
    @Modifying(clearAutomatically = true)
    void markAllCheckedAsTrueByAccount(Long accountId);
}
