package com.seongje.studyolle.modules.notification;

import com.seongje.studyolle.infra.config.AppProperties;
import com.seongje.studyolle.infra.mail.EmailMessage;
import com.seongje.studyolle.infra.mail.MailService;
import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.study.domain.Study;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
@RequiredArgsConstructor
public class NotificationMailSender {

    public static final String emailSubjectForCreated = "[스터디 올래] 새로 오픈된 스터디 소식입니다.";
    public static final String emailSubjectForUpdated = "[스터디 올래] 참여 중인 스터디 관련 소식입니다.";
    public static final String emailSubjectForEnrollmentResult = "[스터디 올래] 모임 신청 및 결과 관련 소식입니다.";

    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final MailService mailService;

    public void sendNotificationEmail(Study study, Account account, String contextMessage, String emailSubject, boolean isDeleted) {
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
}
