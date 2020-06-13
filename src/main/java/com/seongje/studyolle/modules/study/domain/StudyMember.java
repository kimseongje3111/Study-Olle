package com.seongje.studyolle.modules.study.domain;

import com.seongje.studyolle.modules.account.domain.Account;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "study_member")
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class StudyMember {

    @Id @GeneratedValue
    @Column(name = "study_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Enumerated(EnumType.STRING)
    private ManagementLevel managementLevel;

    // 생성 메서드 //

    public static StudyMember createStudyMember(Study study, Account account, ManagementLevel managementLevel) {
        return StudyMember.builder()
                .study(study)
                .account(account)
                .managementLevel(managementLevel)
                .build();
    }
}
