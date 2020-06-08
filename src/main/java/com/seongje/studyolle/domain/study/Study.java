package com.seongje.studyolle.domain.study;

import com.seongje.studyolle.domain.Tag;
import com.seongje.studyolle.domain.Zone;
import com.seongje.studyolle.domain.account.Account;
import com.seongje.studyolle.modules.authentication.UserAccount;
import lombok.*;

import javax.persistence.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static com.seongje.studyolle.domain.study.ManagementLevel.*;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@AllArgsConstructor @NoArgsConstructor
public class Study {

    @Id @GeneratedValue
    @Column(name = "study_id")
    private Long id;

    // 스터디 기본 정보 //

    private String title;

    @Column(unique = true)
    private String path;

    private String shortDescription;

    @Lob
    private String fullDescription;

    @Lob
    private String bannerImg;

    private int memberCount = 0;

    // 스터디 설정 정보 //

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingUpdatedDateTime;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudyTagItem> tags = new HashSet<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudyZoneItem> zones = new HashSet<>();

    // 관리자 및 참여 멤버 //

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private Set<StudyMember> members = new HashSet<>();

    // 연관 관계 편의 메서드 //

    public void addStudyMember(StudyMember studyMember) {
        memberCount++;
        this.members.add(studyMember);
        studyMember.setStudy(this);
    }

    public void removeStudyMember(Account account) {
        memberCount--;
        this.members.removeIf(studyMember -> studyMember.getAccount().equals(account));
    }

    public void addTagItem(StudyTagItem studyTagItem) {
        this.tags.add(studyTagItem);
        studyTagItem.setStudy(this);
    }

    public void removeTagItem(Tag tag) {
        this.tags.removeIf(studyTagItem -> studyTagItem.getTag().equals(tag));
    }

    public void addZoneItem(StudyZoneItem studyZoneItem) {
        this.zones.add(studyZoneItem);
        studyZoneItem.setStudy(this);
    }

    public void removeZoneItem(Zone zone) {
        this.zones.removeIf(studyZoneItem -> studyZoneItem.getZone().equals(zone));
    }

    // 비지니스 메서드 //

    public boolean canJoin(UserAccount userAccount) {
        Account currentUser = userAccount.getAccount();

        if (!this.isPublished() || !this.isRecruiting() || this.closed) {
            return false;
        }

        for (StudyMember studyMember : this.members) {
            if (currentUser.equals(studyMember.getAccount())) {
                return false;
            }
        }

        return true;
    }

    public boolean isMember(UserAccount userAccount) {
        Account currentUser = userAccount.getAccount();

        for (StudyMember studyMember : this.members) {
            if (currentUser.equals(studyMember.getAccount())) {
                return (studyMember.getManagementLevel() == MEMBER);
            }
        }

        return false;
    }

    public boolean isManager(UserAccount userAccount) {
        Account currentUser = userAccount.getAccount();

        for (StudyMember studyMember : this.members) {
            if (currentUser.equals(studyMember.getAccount())) {
                return (studyMember.getManagementLevel() == MANAGER);
            }
        }

        return false;
    }

    public boolean isManagerBy(Account account) {
        for (StudyMember studyMember : this.members) {
            if (account.equals(studyMember.getAccount())) {
                return (studyMember.getManagementLevel() == MANAGER);
            }
        }

        return false;
    }

    public String getEncodedPath() throws UnsupportedEncodingException {
        return URLEncoder.encode(this.path, "UTF-8");
    }

    public void publish() {
        if (!this.closed && !this.published) {
            this.published = true;
            this.publishedDateTime = LocalDateTime.now();

        } else {
            throw new RuntimeException("스터디를 공개할 수 없습니다. 이미 공개됬거나 종료된 스터디입니다.");
        }
    }

    public void close() {
        if (!this.closed && this.published) {
            this.closed = true;
            this.closedDateTime = LocalDateTime.now();

        } else {
            throw new RuntimeException("스터디를 종료할 수 없습니다. 아직 공개되지 않았거나 이미 종료된 스터디입니다.");
        }
    }

    public boolean canUpdateRecruiting() {
        return this.published && (this.recruitingUpdatedDateTime == null ||
                this.recruitingUpdatedDateTime.isBefore(LocalDateTime.now().minusHours(1)));
    }

    public void startRecruiting() {
        if (!canUpdateRecruiting()) {
            throw new RuntimeException("팀원 모집을 시작할 수 없습니다. 스터디를 공개하거나 1시간 뒤에 시도하세요.");
        }

        this.recruiting = true;
        this.recruitingUpdatedDateTime = LocalDateTime.now();
    }

    public void stopRecruiting() {
        if (!canUpdateRecruiting()) {
            throw new RuntimeException("팀원 모집을 시작할 수 없습니다. 스터디를 공개하거나 1시간 뒤에 시도하세요.");
        }

        this.recruiting = false;
        this.recruitingUpdatedDateTime = LocalDateTime.now();
    }

    public boolean canDeleteStudy() {
        return !this.published;     // TODO : 이미 모임을 한번 했던 스터디 불가
    }
}
