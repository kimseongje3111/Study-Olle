package com.seongje.studyolle.modules.notification.domain;

import com.seongje.studyolle.modules.account.domain.Account;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Notification {

    @Id @GeneratedValue
    @Column(name = "notification_id")
    private Long id;

    private String title;

    private String link;

    private String message;

    private String additionalExplanation;

    private LocalDateTime createdDateTime;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    private boolean checked;

    private boolean isMyStudy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    // 생성 메서드 ///

    public static Notification createNotification(String title, String link, String message, NotificationType notificationType,
                                                  boolean isMyStudy, Account account) {

        return Notification.builder()
                .title(title)
                .link(link)
                .message(message)
                .createdDateTime(LocalDateTime.now())
                .notificationType(notificationType)
                .checked(false)
                .isMyStudy(isMyStudy)
                .account(account)
                .build();
    }
}
