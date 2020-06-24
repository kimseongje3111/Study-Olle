package com.seongje.studyolle.modules.event.domain;

import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.study.domain.Study;
import com.seongje.studyolle.modules.account.authentication.UserAccount;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.seongje.studyolle.modules.event.domain.EventType.*;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

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

    @Lob @Basic(fetch = FetchType.EAGER)
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

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    @OrderBy("appliedDateTime")
    private List<Enrollment> enrollments = new ArrayList<>();

    // 연관 관계 편의 메서드 //

    public void addEnrollment(Enrollment enrollment) {
        this.enrollments.add(enrollment);
        enrollment.setEvent(this);
    }

    public void removeEnrollment(Enrollment enrollment) {
        this.enrollments.remove(enrollment);
    }

    // 비지니스 메서드 //

    public Event newEvent(Account account, Study study) {
        this.createdBy = account;
        this.study = study;
        this.createdDateTime = LocalDateTime.now();

        return this;
    }

    public boolean isEnrollableFor(UserAccount userAccount) {
        return !isAfterEnrollmentDeadline() && !isAttended(userAccount) && !isApplied(userAccount);
    }

    public boolean isDisenrollableFor(UserAccount userAccount) {
        return !isStarted() && !isAttended(userAccount) && isApplied(userAccount);
    }

    public boolean isApplied(UserAccount userAccount) {
        return this.enrollments.stream()
                .map(Enrollment::getAccount)
                .collect(toSet())
                .contains(userAccount.getAccount());
    }

    public boolean isAttended(UserAccount userAccount) {
        return this.enrollments.stream()
                .filter(enrollment -> enrollment.getAccount().equals(userAccount.getAccount())
                        && enrollment.isApproved() && enrollment.isAttended())
                .count() == 1;
    }

    public boolean isNotAttended(UserAccount userAccount) {
        return this.enrollments.stream()
                .filter(enrollment -> enrollment.getAccount().equals(userAccount.getAccount())
                        && enrollment.isApproved() && !enrollment.isAttended())
                .count() == 1;
    }

    public boolean isStarted() {
        return LocalDateTime.now().isAfter(this.startDateTime);
    }

    public boolean isClosed() {
        return LocalDateTime.now().isAfter(this.endDateTime);
    }

    public boolean isAfterEnrollmentDeadline() {
        return LocalDateTime.now().isAfter(this.enrollmentDeadline);
    }

    public boolean canAccept(Enrollment enrollment) {
        return !isAfterEnrollmentDeadline()
                && this.eventType == MANAGER_APPROVAL
                && this.enrollments.contains(enrollment)
                && !this.createdBy.equals(enrollment.getAccount())
                && (getNumberOfRemainingSeats() > 0)
                && !enrollment.isAttended()
                && !enrollment.isApproved();
    }

    public boolean canReject(Enrollment enrollment) {
        return !isAfterEnrollmentDeadline()
                && this.eventType == MANAGER_APPROVAL
                && this.enrollments.contains(enrollment)
                && !this.createdBy.equals(enrollment.getAccount())
                && !enrollment.isAttended()
                && enrollment.isApproved();
    }

    public boolean isRemainingSeatsFor(EventType eventType) {
        return this.eventType == eventType && (getNumberOfRemainingSeats() > 0);
    }

    public int getNumberOfRemainingSeats() {
        return this.limitOfEnrollments - (int) getNumberOfApprovedEnrollments();
    }

    public long getNumberOfApprovedEnrollments() {
        return this.enrollments.stream().filter(Enrollment::isApproved).count();
    }

    public List<Enrollment> getWaitingEnrollmentsOfRemainingSeats() {
        return this.enrollments.stream()
                .filter(enrollment -> !enrollment.isApproved())
                .sorted(comparing(Enrollment::getAppliedDateTime))
                .limit(getNumberOfRemainingSeats())
                .collect(toList());
    }
}
