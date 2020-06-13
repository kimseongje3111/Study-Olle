package com.seongje.studyolle.infra.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile({"local", "test"})
@Slf4j
@Component
public class ConsoleMailService implements MailService {

    @Override
    public void send(EmailMessage emailMessage) {
        log.info("Complete mail send : {}", emailMessage.getText());
    }
}
