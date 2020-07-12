package com.seongje.studyolle.modules.event.app_event;

import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.event.app_event.custom.event.EventAppEvent;
import com.seongje.studyolle.modules.event.app_event.custom.event.EventCreatedEvent;
import com.seongje.studyolle.modules.event.app_event.custom.event.EventUpdatedEvent;
import com.seongje.studyolle.modules.notification.NotificationMailSender;
import com.seongje.studyolle.modules.notification.repository.NotificationRepository;
import com.seongje.studyolle.modules.study.domain.Study;
import com.seongje.studyolle.modules.study.repository.StudyMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.seongje.studyolle.modules.notification.NotificationMailSender.*;
import static com.seongje.studyolle.modules.notification.domain.Notification.createNotification;
import static com.seongje.studyolle.modules.notification.domain.NotificationType.STUDY_UPDATED;
import static com.seongje.studyolle.modules.study.domain.ManagementLevel.*;
import static java.lang.String.*;

@Async
@Component
@RequiredArgsConstructor
@Transactional
public class EventAppEventListener {

    private final NotificationRepository notificationRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final NotificationMailSender mailSender;

    @SneakyThrows
    @EventListener
    public void handleEventAppEvent(EventAppEvent appEvent) {
        Study study = appEvent.getEvent().getStudy();
        String title = study.getTitle();
        String link = "/study/studies/" + study.getEncodedPath();

        studyMemberRepository.searchMembersByStudy(study.getId()).forEach(studyMember -> {
            if (studyMember.getManagementLevel() == MANAGER) {
                Account manager = studyMember.getAccount();

                setMessageContentForManagerByInstanceOf(appEvent);

                if (manager.isStudyUpdatedByWeb()) {
                    notificationRepository.save(
                            createNotification(title, link, appEvent.getMessage(), STUDY_UPDATED, true, manager)
                    );
                }

                if (manager.isStudyUpdatedByEmail()) {
                    mailSender.sendNotificationEmail(study, manager, appEvent.getMessage(), emailSubjectForUpdated, false);
                }

            } else {
                Account member = studyMember.getAccount();

                setMessageContentForMemberByInstanceOf(appEvent);

                if (member.isStudyUpdatedByWeb()) {
                    notificationRepository.save(
                            createNotification(title, link, appEvent.getMessage(), STUDY_UPDATED, false, member)
                    );
                }

                if (member.isStudyUpdatedByEmail()) {
                    mailSender.sendNotificationEmail(study, member, appEvent.getMessage(), emailSubjectForUpdated, false);
                }
            }
        });
    }

    private void setMessageContentForManagerByInstanceOf(EventAppEvent appEvent) {
        if (appEvent instanceof EventCreatedEvent) {
            appEvent.setMessageContent(format("'%s' 모임을 만들었습니다.", appEvent.getEvent().getTitle()));

        } else if (appEvent instanceof EventUpdatedEvent) {
            appEvent.setMessageContent(format("'%s' 모임을 수정하였습니다.", appEvent.getEvent().getTitle()));

        } else {
            appEvent.setMessageContent(format("'%s' 모임을 취소하였습니다.", appEvent.getEvent().getTitle()));
        }
    }

    private void setMessageContentForMemberByInstanceOf(EventAppEvent appEvent) {
        if (appEvent instanceof EventCreatedEvent) {
            appEvent.setMessageContent(format("'%s' 모임이 새로 등록되었습니다. 모임에 참여해보세요.", appEvent.getEvent().getTitle()));

        } else if (appEvent instanceof EventUpdatedEvent) {
            appEvent.setMessageContent(format("'%s' 모임이 수정되었습니다. 변경 내용을 확인해주세요..", appEvent.getEvent().getTitle()));

        } else {
            appEvent.setMessageContent(format("'%s' 모임이 취소되었습니다.", appEvent.getEvent().getTitle()));
        }
    }
}
