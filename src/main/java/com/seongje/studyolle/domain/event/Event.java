package com.seongje.studyolle.domain.event;

import com.seongje.studyolle.domain.account.Account;
import com.seongje.studyolle.domain.study.Study;
import com.seongje.studyolle.modules.authentication.UserAccount;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@AllArgsConstructor @NoArgsConstructor
public class Event {

    @Id @GeneratedValue
    @Column(name = "event_id")
    private Long id;

    // 모임 기본 정보 //

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    private Integer limitOfEnrollments;

    private LocalDateTime createdDateTime;

    private LocalDateTime enrollmentDeadline;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account createdBy;

    // 참여 인원 정보 //

    @OneToMany(mappedBy = "event")
    private List<Enrollment> enrollments = new ArrayList<>();

    // 연관 관계 편의 메서드 //

    public void addEnrollment(Enrollment enrollment) {
        this.enrollments.add(enrollment);
        enrollment.setEvent(this);
    }

    public void removeEnrollment(Enrollment enrollment) {
        this.enrollments.remove(enrollment);
        enrollment.setEvent(null);
    }

    // 비지니스 메서드 //

    public Event newEvent(Account account, Study study) {
        this.createdBy = account;
        this.study = study;
        this.createdDateTime = LocalDateTime.now();

        return this;
    }

    public boolean isEnrollableFor(UserAccount userAccount) {
        return !isClosed() && !isAttended(userAccount) && !isApplied(userAccount);
    }

    public boolean isDisenrollableFor(UserAccount userAccount) {
        return !isClosed() && !isAttended(userAccount) && isApplied(userAccount);
    }

    public boolean isAttended(UserAccount userAccount) {
        Account account = userAccount.getAccount();

        for (Enrollment enrollment : this.enrollments) {
            if (enrollment.getAccount().equals(account) && enrollment.isAttended()) {
                return true;
            }
        }

        return false;
    }

    public boolean isClosed() {
        return LocalDateTime.now().isAfter(this.endDateTime);
    }

    public boolean canAccept(Enrollment enrollment) {
        return this.eventType == EventType.MANAGER_APPROVAL
                && this.enrollments.contains(enrollment)
                && (getNumberOfRemainingSeat() > 0)
                && !enrollment.isAttended()
                && !enrollment.isApproved();
    }

    public boolean canReject(Enrollment enrollment) {
        return this.eventType == EventType.MANAGER_APPROVAL
                && this.enrollments.contains(enrollment)
                && !enrollment.isAttended()
                && enrollment.isApproved();
    }

    public int getNumberOfRemainingSeat() {
        return this.limitOfEnrollments - (int) getNumberOfAcceptedEnrollments();
    }

    public long getNumberOfAcceptedEnrollments() {
        return this.enrollments.stream().filter(Enrollment::isApproved).count();
    }

    public void updateEnrollmentsWaitingToApproved() {
        this.enrollments.stream()
                .filter(enrollment -> !enrollment.isApproved())
                .sorted(Comparator.comparing(Enrollment::getAppliedDateTime))
                .limit(getNumberOfRemainingSeat())
                .forEach(enrollment -> enrollment.setApproved(true));
    }

    private boolean isApplied(UserAccount userAccount) {
        Account account = userAccount.getAccount();

        for (Enrollment enrollment : this.enrollments) {
            if (enrollment.getAccount().equals(account)) {
                return true;
            }
        }

        return false;
    }
}
