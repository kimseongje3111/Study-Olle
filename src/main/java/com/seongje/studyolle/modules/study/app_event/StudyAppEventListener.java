package com.seongje.studyolle.modules.study.app_event;

import com.seongje.studyolle.infra.config.AppProperties;
import com.seongje.studyolle.infra.mail.EmailMessage;
import com.seongje.studyolle.infra.mail.MailService;
import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.account.repository.AccountRepository;
import com.seongje.studyolle.modules.notification.domain.Notification;
import com.seongje.studyolle.modules.notification.repository.NotificationRepository;
import com.seongje.studyolle.modules.study.app_event.custom.*;
import com.seongje.studyolle.modules.study.domain.*;
import com.seongje.studyolle.modules.study.repository.StudyRepository;
import com.seongje.studyolle.modules.tag.domain.Tag;
import com.seongje.studyolle.modules.zone.domain.Zone;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    private static final String emailSubjectForCreated = "[스터디 올래] 새로 오픈된 스터디 소식입니다.";
    private static final String emailSubjectForUpdated = "[스터디 올래] 참여 중인 스터디 관련 소식입니다.";

    private final StudyRepository studyRepository;
    private final AccountRepository accountRepository;
    private final NotificationRepository notificationRepository;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final MailService mailService;

    @SneakyThrows
    @EventListener
    public void handleStudyCreatedEvent(StudyCreatedEvent appEvent) {
        Study findStudy = studyRepository.findByPath(appEvent.getStudy().getPath());
        String title = findStudy.getTitle();
        String link = "/study/studies/" + findStudy.getEncodedPath();

        Set<Tag> studyTags = findStudy.getTags().stream().map(StudyTagItem::getTag).collect(Collectors.toSet());
        Set<Zone> studyZones = findStudy.getZones().stream().map(StudyZoneItem::getZone).collect(Collectors.toSet());
        List<Account> accountList = accountRepository.searchAllByTagsAndZones(studyTags, studyZones);

        findStudy.getMembers().forEach(studyMember -> {
            if (studyMember.getManagementLevel() == MANAGER) {
                Account manager = studyMember.getAccount();

                appEvent.setMessageContent("스터디를 공개하였습니다. 이제 팀원을 모집해보세요.");

                if (manager.isStudyUpdatedByWeb()) {
                    notificationRepository.save(
                            createNotification(title, link, appEvent.getMessage(), STUDY_UPDATED, true, manager)
                    );
                }

                if (manager.isStudyUpdatedByEmail()) {
                    sendNotificationEmail(findStudy, manager, appEvent.getMessage(), emailSubjectForUpdated, false);
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
                sendNotificationEmail(findStudy, account, appEvent.getMessage(), emailSubjectForCreated, false);
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
                    sendNotificationEmail(deletedStudy, manager, appEvent.getMessage(), emailSubjectForUpdated, true);
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
                    sendNotificationEmail(deletedStudy, member, appEvent.getMessage(), emailSubjectForUpdated, true);
                }
            }
        });
    }

    @SneakyThrows
    @EventListener
    public void handleStudyMemberEvent(StudyMemberEvent appEvent) {
        Account findAccount = accountRepository.findByEmail(appEvent.getAccount().getEmail());
        Study findStudy = studyRepository.findByPath(appEvent.getStudy().getPath());
        String title = findStudy.getTitle();
        String link = "/study/studies/" + findStudy.getEncodedPath();

        findStudy.getMembers().forEach(studyMember -> {
            if (studyMember.getManagementLevel() == MANAGER) {
                Account manager = studyMember.getAccount();

                if (appEvent.getEventType() == JOIN) {
                    appEvent.setMessageContent("스터디에 새로운 팀원이 가입하였습니다.");

                } else if (appEvent.getEventType() == LEAVE) {
                    appEvent.setMessageContent("스터디에서 탈퇴한 팀원이 있습니다.");
                }

                if (manager.isStudyUpdatedByWeb()) {
                    Notification newNotification = createNotification(title, link, appEvent.getMessage(), STUDY_UPDATED, true, manager);
                    newNotification.setAdditionalExplanation("사용자 : " + findAccount.getNickname());

                    notificationRepository.save(newNotification);
                }

                if (manager.isStudyUpdatedByEmail()) {
                    sendNotificationEmail(findStudy, manager, appEvent.getMessage(), emailSubjectForUpdated, false);
                }
            }
        });

        if (appEvent.getEventType() == JOIN) {
            appEvent.setMessageContent("스터디에 가입하였습니다. 이제부터 해당 스터디에서 활동할 수 있습니다.");

        } else if (appEvent.getEventType() == LEAVE) {
            appEvent.setMessageContent("스터디에서 탈퇴하였습니다. 새로운 스터디를 찾아보세요.");
        }

        if (findAccount.isStudyUpdatedByWeb()) {
            notificationRepository.save(
                    createNotification(title, link, appEvent.getMessage(), STUDY_UPDATED, false, findAccount)
            );
        }

        if (findAccount.isStudyUpdatedByEmail()) {
            sendNotificationEmail(findStudy, findAccount, appEvent.getMessage(), emailSubjectForUpdated, false);
        }
    }

    @SneakyThrows
    @EventListener
    public void handleStudyUpdatedEvent(StudyUpdatedEvent appEvent) {
        Study findStudy = studyRepository.findByPath(appEvent.getStudy().getPath());
        String title = findStudy.getTitle();
        String link = "/study/studies/" + findStudy.getEncodedPath();

        findStudy.getMembers().forEach(studyMember -> {
            if (studyMember.getManagementLevel() == MANAGER) {
                Account manager = studyMember.getAccount();

                setMessageContentForManagerByUpdatedType(appEvent);

                if (manager.isStudyUpdatedByWeb()) {
                    Notification newNotification;

                    if (appEvent.getEventType() == TITLE) {
                        newNotification = createNotification(appEvent.getNewTitle(), link, appEvent.getMessage(), STUDY_UPDATED, true, manager);
                        newNotification.setAdditionalExplanation("변경 전 : " + title);

                    } else if (appEvent.getEventType() == PATH) {
                        String newLink = "/study/studies/" + appEvent.getNewPath();

                        newNotification = createNotification(title, newLink, appEvent.getMessage(), STUDY_UPDATED, true, manager);
                        newNotification.setAdditionalExplanation("변경 전 : " + link);

                    } else {
                        newNotification = createNotification(title, link, appEvent.getMessage(), STUDY_UPDATED, true, manager);
                    }

                    notificationRepository.save(newNotification);
                }

                if (manager.isStudyUpdatedByEmail()) {
                    sendNotificationEmail(findStudy, manager, appEvent.getMessage(), emailSubjectForUpdated, false);
                }

            } else {
                Account member = studyMember.getAccount();

                setMessageContentForMemberByUpdatedType(appEvent);

                if (member.isStudyUpdatedByWeb()) {
                    Notification newNotification;

                    if (appEvent.getEventType() == TITLE) {
                        newNotification = createNotification(appEvent.getNewTitle(), link, appEvent.getMessage(), STUDY_UPDATED, false, member);
                        newNotification.setAdditionalExplanation("변경 전 : " + title);

                    } else if (appEvent.getEventType() == PATH) {
                        String newLink = "/study/studies/" + appEvent.getNewPath();

                        newNotification = createNotification(title, newLink, appEvent.getMessage(), STUDY_UPDATED, false, member);
                        newNotification.setAdditionalExplanation("변경 전 : " + link);

                    } else {
                        newNotification = createNotification(title, link, appEvent.getMessage(), STUDY_UPDATED, false, member);
                    }

                    notificationRepository.save(newNotification);
                }

                if (member.isStudyUpdatedByEmail()) {
                    sendNotificationEmail(findStudy, member, appEvent.getMessage(), emailSubjectForUpdated, false);
                }
            }
        });
    }

    private void sendNotificationEmail(Study study, Account account, String contextMessage, String emailSubject, boolean isDeleted) {
        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject(emailSubject)
                .text(getNotificationEmailContent(study, account, contextMessage, isDeleted))
                .build();

        mailService.send(emailMessage);
    }

    @SneakyThrows
    private String getNotificationEmailContent(Study study, Account account, String contextMessage, boolean isDeleted){
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("message", contextMessage);
        context.setVariable("linkName", study.getTitle());
        context.setVariable("link", "/study/studies/" + study.getEncodedPath());
        context.setVariable("host", appProperties.getHost());
        context.setVariable("isDeleted", isDeleted);

        return templateEngine.process("mail/notification-email-template", context);
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

