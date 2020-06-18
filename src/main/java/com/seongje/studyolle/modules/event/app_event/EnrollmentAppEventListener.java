package com.seongje.studyolle.modules.event.app_event;

import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.event.app_event.custom.enrollment.*;
import com.seongje.studyolle.modules.event.domain.Event;
import com.seongje.studyolle.modules.notification.NotificationMailSender;
import com.seongje.studyolle.modules.notification.domain.Notification;
import com.seongje.studyolle.modules.notification.domain.NotificationType;
import com.seongje.studyolle.modules.notification.repository.NotificationRepository;
import com.seongje.studyolle.modules.study.domain.Study;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.seongje.studyolle.modules.event.app_event.custom.enrollment.EnrollmentResultType.*;
import static com.seongje.studyolle.modules.notification.NotificationMailSender.emailSubjectForEnrollmentResult;
import static com.seongje.studyolle.modules.notification.NotificationMailSender.emailSubjectForUpdated;
import static com.seongje.studyolle.modules.notification.domain.Notification.createNotification;
import static com.seongje.studyolle.modules.notification.domain.NotificationType.*;
import static com.seongje.studyolle.modules.notification.domain.NotificationType.STUDY_UPDATED;
import static java.lang.String.*;

@Async
@Component
@RequiredArgsConstructor
@Transactional
public class EnrollmentAppEventListener {

    private final NotificationRepository notificationRepository;
    private final NotificationMailSender mailSender;

    @SneakyThrows
    @EventListener
    public void handleEnrollmentAppliedEvent(EnrollmentAppliedEvent appEvent) {
        Event event = appEvent.getEnrollment().getEvent();
        Study study = event.getStudy();
        String title = study.getTitle();
        String link = "/study/studies/" + study.getEncodedPath();

        Account manager = event.getCreatedBy();
        Account member = appEvent.getEnrollment().getAccount();

        if (!manager.equals(member)) {
            if (appEvent.getEventType() == APPROVED) {
                appEvent.setMessageContent(format("'%s' 모임 확정자입니다. 자세한 내용은 모임 참가 신청 현황을 확인해주세요", event.getTitle()));

                if (manager.isStudyUpdatedByWeb()) {
                    Notification newNotification = createNotification(title, link, appEvent.getMessage(), EVENT_ENROLLMENT_RESULT, true, manager);
                    newNotification.setAdditionalExplanation("사용자 : " + member.getNickname());

                    notificationRepository.save(newNotification);
                }

                if (manager.isStudyUpdatedByEmail()) {
                    mailSender.sendNotificationEmail(study, manager, appEvent.getMessage(), emailSubjectForEnrollmentResult, false);
                }
            }

            setMessageContentForMemberByEnrollmentResultType(appEvent, event.getTitle());

            if (member.isStudyUpdatedByWeb()) {
                notificationRepository.save(
                        createNotification(title, link, appEvent.getMessage(), EVENT_ENROLLMENT_RESULT, false, member)
                );
            }

            if (member.isStudyUpdatedByEmail()) {
                mailSender.sendNotificationEmail(study, member, appEvent.getMessage(), emailSubjectForEnrollmentResult, false);
            }
        }
    }

    @SneakyThrows
    @EventListener
    public void handleEnrollmentCancelledEvent(EnrollmentCancelledEvent appEvent) {
        Event event = appEvent.getEnrollment().getEvent();
        Study study = event.getStudy();
        String title = study.getTitle();
        String link = "/study/studies/" + study.getEncodedPath();

        Account manager = event.getCreatedBy();
        Account member = appEvent.getEnrollment().getAccount();

        if (!manager.equals(member)) {
            appEvent.setMessageContent(format("'%s' 모임 신청 취소자가 발생하였습니다.", event.getTitle()));

            if (manager.isStudyUpdatedByWeb()) {
                Notification newNotification = createNotification(title, link, appEvent.getMessage(), EVENT_ENROLLMENT_RESULT, true, manager);
                newNotification.setAdditionalExplanation("사용자 : " + member.getNickname());

                notificationRepository.save(newNotification);
            }

            if (manager.isStudyUpdatedByEmail()) {
                mailSender.sendNotificationEmail(study, manager, appEvent.getMessage(), emailSubjectForEnrollmentResult, false);
            }

            appEvent.setMessageContent(format("'%s' 모임 신청을 취소하였습니다.", event.getTitle()));

            if (member.isStudyUpdatedByWeb()) {
                notificationRepository.save(
                        createNotification(title, link, appEvent.getMessage(), EVENT_ENROLLMENT_RESULT, false, member)
                );
            }

            if (member.isStudyUpdatedByEmail()) {
                mailSender.sendNotificationEmail(study, member, appEvent.getMessage(), emailSubjectForEnrollmentResult, false);
            }
        }
    }

    @EventListener
    public void handleEnrollmentScheduledEvent(EnrollmentScheduledEvent appEvent) {

    }

    private void setMessageContentForMemberByEnrollmentResultType(EnrollmentAppliedEvent appEvent, String title) {
        switch (appEvent.getEventType()) {
            case APPROVED:
                appEvent.setMessageContent(format("'%s' 모임 신청이 확정되었습니다. 모임 일정을 확인하세요.", title));
                break;

            case WAITING:
                appEvent.setMessageContent(format("'%s' 모임 신청을 완료하였습니다. 확정 시 알람이 발송됩니다.", title));
                break;

            case REJECTED:
                appEvent.setMessageContent(format("'%s' 모임 신청이 거절되었습니다. 모임 관리자에게 문의해주세요.", title));
                break;
        }
    }
}
