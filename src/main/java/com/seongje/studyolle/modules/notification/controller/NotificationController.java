package com.seongje.studyolle.modules.notification.controller;

import com.seongje.studyolle.modules.account.authentication.CurrentUser;
import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.notification.domain.Notification;
import com.seongje.studyolle.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class NotificationController {

    static final String REDIRECT = "redirect:/";
    static final String NOTIFICATIONS = "notifications";
    static final String LIST_READ = NOTIFICATIONS + "/read";
    static final String CHANGE_TO_READ = NOTIFICATIONS + "/change-to-read";
    static final String DELETE = NOTIFICATIONS + "/delete";

    private final NotificationService notificationService;

    @GetMapping(NOTIFICATIONS)
    public String getUnreadNotifications(@CurrentUser Account account, Model model) {
        List<Notification> findNotifications = notificationService.getAccountUnreadNotifications(account);
        long numberOfChecked = notificationService.getNumberOfReadNotifications(account);

        classificationByNotificationType(findNotifications, model);

        model.addAttribute(account);
        model.addAttribute("numberOfNotChecked", findNotifications.size());
        model.addAttribute("numberOfChecked", numberOfChecked);
        model.addAttribute("isRead", false);

        return "notification/list-view";
    }

    @GetMapping(LIST_READ)
    public String getReadNotifications(@CurrentUser Account account, Model model) {
        List<Notification> findNotifications = notificationService.getAccountReadNotifications(account);
        long numberOfNotChecked = notificationService.getNumberOfUnreadNotifications(account);

        classificationByNotificationType(findNotifications, model);

        model.addAttribute(account);
        model.addAttribute("numberOfNotChecked", numberOfNotChecked);
        model.addAttribute("numberOfChecked", findNotifications.size());
        model.addAttribute("isRead", true);

        return "notification/list-view";
    }

    @PostMapping(CHANGE_TO_READ)
    public String changeAllNotificationsFromUnreadToRead(@CurrentUser Account account) {
        notificationService.changeAllNotificationsFromUnreadToRead(account);

        return REDIRECT + NOTIFICATIONS;
    }

    @PostMapping(DELETE)
    public String deleteReadNotifications(@CurrentUser Account account) {
        notificationService.deleteReadNotifications(account);

        return REDIRECT + NOTIFICATIONS;
    }

    private void classificationByNotificationType(List<Notification> notifications, Model model) {
        List<Notification> newStudyNotifications = new ArrayList<>();
        List<Notification> eventEnrollmentNotifications = new ArrayList<>();
        List<Notification> participatingStudyNotifications = new ArrayList<>();

        notifications.forEach(notification -> {
            switch (notification.getNotificationType()) {
                case STUDY_CREATED:
                    newStudyNotifications.add(notification);
                    break;

                case STUDY_UPDATED:
                    participatingStudyNotifications.add(notification);
                    break;

                case EVENT_ENROLLMENT_RESULT:
                    eventEnrollmentNotifications.add(notification);
                    break;
            }
        });

        model.addAttribute("notifications", notifications);
        model.addAttribute("newStudyNotifications", newStudyNotifications);
        model.addAttribute("eventEnrollmentNotifications", eventEnrollmentNotifications);
        model.addAttribute("participatingStudyNotifications", participatingStudyNotifications);
    }
}
