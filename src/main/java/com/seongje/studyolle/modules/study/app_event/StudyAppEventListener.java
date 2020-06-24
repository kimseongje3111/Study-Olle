package com.seongje.studyolle.modules.study.app_event;

import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.account.repository.AccountRepository;
import com.seongje.studyolle.modules.notification.NotificationMailSender;
import com.seongje.studyolle.modules.notification.domain.Notification;
import com.seongje.studyolle.modules.notification.repository.NotificationRepository;
import com.seongje.studyolle.modules.study.app_event.custom.*;
import com.seongje.studyolle.modules.study.domain.*;
import com.seongje.studyolle.modules.study.service.StudyService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.seongje.studyolle.modules.notification.NotificationMailSender.*;
import static com.seongje.studyolle.modules.notification.domain.Notification.*;
import static com.seongje.studyolle.modules.notification.domain.NotificationType.*;
import static com.seongje.studyolle.modules.study.app_event.custom.StudyMemberEventType.*;
import static com.seongje.studyolle.modules.study.app_event.custom.StudyUpdatedEventType.*;
import static com.seongje.studyolle.modules.study.domain.ManagementLevel.*;

@Async
@Component
@RequiredArgsConstructor
@Transactional
public class StudyAppEventListener {

    private final StudyService studyService;
    private final AccountRepository accountRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationMailSender mailSender;

    @SneakyThrows
    @EventListener
    public void handleStudyCreatedEvent(StudyCreatedEvent appEvent) {
        Study publishedStudy = appEvent.getStudy();
        String title = publishedStudy.getTitle();
        String link = "/study/studies/" + publishedStudy.getEncodedPath();

        List<Account> accountList =
                accountRepository.searchAllByTagsAndZones(
                        studyService.getStudyTags(publishedStudy),
                        studyService.getStudyZones(publishedStudy)
                );

        publishedStudy.getMembers().forEach(studyMember -> {
            if (studyMember.getManagementLevel() == MANAGER) {
                Account manager = studyMember.getAccount();

                appEvent.setMessageContent("스터디를 공개하였습니다. 이제 팀원을 모집해보세요.");

                if (manager.isStudyUpdatedByWeb()) {
                    notificationRepository.save(
                            createNotification(title, link, appEvent.getMessage(), STUDY_UPDATED, true, manager)
                    );
                }

                if (manager.isStudyUpdatedByEmail()) {
                    mailSender.sendNotificationEmail(publishedStudy, manager, appEvent.getMessage(), emailSubjectForUpdated, false);
                }
            }
        });

        accountList.forEach(account -> {
            appEvent.setMessageContent("새로 오픈된 스터디 중 나에게 맞는 스터디를 확인해보세요.");

            if (account.isStudyCreatedByWeb()) {
                notificationRepository.save(
                        createNotification(title, link, appEvent.getMessage(), STUDY_CREATED, false, account)
                );
            }

            if (account.isStudyCreatedByEmail()) {
                mailSender.sendNotificationEmail(publishedStudy, account, appEvent.getMessage(), emailSubjectForCreated, false);
            }
        });
    }

    @SneakyThrows
    @EventListener
    public void handleStudyDeletedEvent(StudyDeletedEvent appEvent) {
        Study deletedStudy = appEvent.getStudy();

        deletedStudy.getMembers().forEach(studyMember -> {
            if (studyMember.getManagementLevel() == MANAGER) {
                Account manager = studyMember.getAccount();

                appEvent.setMessageContent("스터디를 삭제하였습니다.");

                if (manager.isStudyUpdatedByWeb()) {
                    notificationRepository.save(
                            createNotification(deletedStudy.getTitle(), null, appEvent.getMessage(), STUDY_UPDATED, true, manager)
                    );
                }

                if (manager.isStudyUpdatedByEmail()) {
                    mailSender.sendNotificationEmail(deletedStudy, manager, appEvent.getMessage(), emailSubjectForUpdated, true);
                }

            } else {
                Account member = studyMember.getAccount();

                appEvent.setMessageContent("스터디가 삭제되었습니다. 더 이상 해당 스터디를 이용할 수 없습니다.");

                if (member.isStudyUpdatedByWeb()) {
                    notificationRepository.save(
                            createNotification(deletedStudy.getTitle(), null, appEvent.getMessage(), STUDY_UPDATED, false, member)
                    );
                }

                if (member.isStudyUpdatedByEmail()) {
                    mailSender.sendNotificationEmail(deletedStudy, member, appEvent.getMessage(), emailSubjectForUpdated, true);
                }
            }
        });
    }

    @SneakyThrows
    @EventListener
    public void handleStudyMemberEvent(StudyMemberEvent appEvent) {
        Study currentStudy = appEvent.getStudy();
        String title = currentStudy.getTitle();
        String link = "/study/studies/" + currentStudy.getEncodedPath();

        Account currentAccount = appEvent.getAccount();

        currentStudy.getMembers().forEach(studyMember -> {
            if (studyMember.getManagementLevel() == MANAGER) {
                Account manager = studyMember.getAccount();

                if (appEvent.getEventType() == JOIN) {
                    appEvent.setMessageContent("스터디에 새로운 팀원이 가입하였습니다.");

                } else if (appEvent.getEventType() == LEAVE) {
                    appEvent.setMessageContent("스터디에서 탈퇴한 팀원이 있습니다.");
                }

                if (manager.isStudyUpdatedByWeb()) {
                    Notification newNotification = createNotification(title, link, appEvent.getMessage(), STUDY_UPDATED, true, manager);
                    newNotification.setAdditionalExplanation("사용자 : " + currentAccount.getNickname());

                    notificationRepository.save(newNotification);
                }

                if (manager.isStudyUpdatedByEmail()) {
                    mailSender.sendNotificationEmail(currentStudy, manager, appEvent.getMessage(), emailSubjectForUpdated, false);
                }
            }
        });

        if (appEvent.getEventType() == JOIN) {
            appEvent.setMessageContent("스터디에 가입하였습니다. 이제부터 해당 스터디에서 활동할 수 있습니다.");

        } else if (appEvent.getEventType() == LEAVE) {
            appEvent.setMessageContent("스터디에서 탈퇴하였습니다. 새로운 스터디를 찾아보세요.");
        }

        if (currentAccount.isStudyUpdatedByWeb()) {
            notificationRepository.save(
                    createNotification(title, link, appEvent.getMessage(), STUDY_UPDATED, false, currentAccount)
            );
        }

        if (currentAccount.isStudyUpdatedByEmail()) {
            mailSender.sendNotificationEmail(currentStudy, currentAccount, appEvent.getMessage(), emailSubjectForUpdated, false);
        }
    }

    @SneakyThrows
    @EventListener
    public void handleStudyUpdatedEvent(StudyUpdatedEvent appEvent) {
        Study updatedStudy = appEvent.getStudy();
        String title = updatedStudy.getTitle();
        String link = "/study/studies/" + updatedStudy.getEncodedPath();

        updatedStudy.getMembers().forEach(studyMember -> {
            if (studyMember.getManagementLevel() == MANAGER) {
                Account manager = studyMember.getAccount();

                setMessageContentForManagerByUpdatedType(appEvent);

                if (manager.isStudyUpdatedByWeb()) {
                    Notification newNotification;

                    if (appEvent.getEventType() == TITLE) {
                        newNotification = createNotification(title, link, appEvent.getMessage(), STUDY_UPDATED, true, manager);
                        newNotification.setAdditionalExplanation("변경 전 : " + appEvent.getPrevTitle());

                    } else if (appEvent.getEventType() == PATH) {
                        newNotification = createNotification(title, link, appEvent.getMessage(), STUDY_UPDATED, true, manager);
                        newNotification.setAdditionalExplanation("변경 전 : " + appEvent.getPrevPath());

                    } else {
                        newNotification = createNotification(title, link, appEvent.getMessage(), STUDY_UPDATED, true, manager);
                    }

                    notificationRepository.save(newNotification);
                }

                if (manager.isStudyUpdatedByEmail()) {
                    mailSender.sendNotificationEmail(updatedStudy, manager, appEvent.getMessage(), emailSubjectForUpdated, false);
                }

            } else {
                Account member = studyMember.getAccount();

                setMessageContentForMemberByUpdatedType(appEvent);

                if (member.isStudyUpdatedByWeb()) {
                    Notification newNotification;

                    if (appEvent.getEventType() == TITLE) {
                        newNotification = createNotification(title, link, appEvent.getMessage(), STUDY_UPDATED, false, member);
                        newNotification.setAdditionalExplanation("변경 전 : " + appEvent.getPrevTitle());

                    } else if (appEvent.getEventType() == PATH) {
                        newNotification = createNotification(title, link, appEvent.getMessage(), STUDY_UPDATED, false, member);
                        newNotification.setAdditionalExplanation("변경 전 : " + appEvent.getPrevPath());

                    } else {
                        newNotification = createNotification(title, link, appEvent.getMessage(), STUDY_UPDATED, false, member);
                    }

                    notificationRepository.save(newNotification);
                }

                if (member.isStudyUpdatedByEmail()) {
                    mailSender.sendNotificationEmail(updatedStudy, member, appEvent.getMessage(), emailSubjectForUpdated, false);
                }
            }
        });
    }

    private void setMessageContentForManagerByUpdatedType(StudyUpdatedEvent studyUpdatedEvent) {
        switch (studyUpdatedEvent.getEventType()) {
            case CLOSED:
                studyUpdatedEvent.setMessageContent("스터디를 종료하였습니다. 계속 진행하려면 새로운 스터디를 만들어주세요.");
                break;

            case RECRUIT_START:
                studyUpdatedEvent.setMessageContent("이제부터 팀원 모집을 시작합니다. 팀원 모집 상태는 1시간에 한번만 변경할 수 있습니다.");
                break;

            case RECRUIT_STOP:
                studyUpdatedEvent.setMessageContent("팀원 모집을 중단하였습니다. 팀원 모집 상태는 1시간에 한번만 변경할 수 있습니다.");
                break;

            case TITLE:
                studyUpdatedEvent.setMessageContent("스터디의 이름을 변경하였습니다.");
                break;

            case PATH:
                studyUpdatedEvent.setMessageContent("스터디의 경로를 변경하였습니다. 더 이상 변경 전의 경로는 사용할 수 없습니다.");
                break;
        }
    }

    private void setMessageContentForMemberByUpdatedType(StudyUpdatedEvent studyUpdatedEvent) {
        switch (studyUpdatedEvent.getEventType()) {
            case CLOSED:
                studyUpdatedEvent.setMessageContent("스터디가 종료되었습니다. 새로운 스터디를 찾아보세요.");
                break;

            case RECRUIT_START:
                studyUpdatedEvent.setMessageContent("팀원 모집이 시작되었습니다.");
                break;

            case RECRUIT_STOP:
                studyUpdatedEvent.setMessageContent("팀원 모집이 중단되었습니다.");
                break;

            case TITLE:
                studyUpdatedEvent.setMessageContent("스터디의 이름이 변경되었습니다.");
                break;

            case PATH:
                studyUpdatedEvent.setMessageContent("스터디의 경로가 변경되었습니다. 더 이상 변경 전의 경로는 사용할 수 없습니다.");
                break;
        }
    }
}

