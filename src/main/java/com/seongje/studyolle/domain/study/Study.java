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
@Builder @AllArgsConstructor @NoArgsConstructor
public class Study {

    @Id @GeneratedValue
    @Column(name = "study_id")
    private Long id;

    // 관리자 및 참여 멤버 //

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private Set<StudyMember> members = new HashSet<>();

    // 스터디 기본 정보 //

    private String title;

    @Column(unique = true)
    private String path;

    private String shortDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String fullDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
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

    // 연관 관계 편의 메서드 //

    public void addStudyMember(StudyMember studyMember) {
        memberCount++;
        this.members.add(studyMember);
        studyMember.setStudy(this);
    }

    // 비지니스 메서드 //

    public boolean isJoinable(UserAccount userAccount) {
        Account currentUser = userAccount.getAccount();

        if (!this.isPublished() || !this.isRecruiting()) {
            return false;
        }

        for (StudyMember studyMember : this.members) {
            if (!currentUser.equals(studyMember.getAccount())) {
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
}
