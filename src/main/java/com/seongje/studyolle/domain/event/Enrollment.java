package com.seongje.studyolle.domain.event;

import com.seongje.studyolle.domain.account.Account;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Enrollment {

    @Id @GeneratedValue
    @Column(name = "enrollment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    private LocalDateTime appliedDateTime;

    private boolean approved;

    private boolean attended;

    public static Enrollment createNewEnrollment(Event event, Account account) {
        return Enrollment.builder()
                .event(event)
                .account(account)
                .appliedDateTime(LocalDateTime.now())
                .build();
    }
}
