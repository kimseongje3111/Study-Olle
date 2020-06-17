package com.seongje.studyolle.modules.notification.service;

import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.notification.domain.Notification;
import com.seongje.studyolle.modules.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<Notification> getAccountUnreadNotifications(Account account) {
        return notificationRepository
                .searchAllByAccountAndCheckedAndCreatedDateTimeDesc(account.getId(), false);
    }

    public List<Notification> getAccountReadNotifications(Account account) {
        return notificationRepository
                .searchAllByAccountAndCheckedAndCreatedDateTimeDesc(account.getId(), true);
    }

    public long getNumberOfReadNotifications(Account account) {
        return notificationRepository.countByAccountAndChecked(account, true);
    }

    public long getNumberOfUnreadNotifications(Account account) {
        return notificationRepository.countByAccountAndChecked(account, false);
    }

    @Transactional
    public void changeAllNotificationsFromUnreadToRead(Account account) {
        notificationRepository.markAllCheckedAsTrueByAccount(account.getId());
    }

    @Transactional
    public void deleteReadNotifications(Account account) {
        notificationRepository.deleteByAccountAndChecked(account, true);
    }
}
