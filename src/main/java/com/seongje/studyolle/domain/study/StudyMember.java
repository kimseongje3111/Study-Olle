package com.seongje.studyolle.domain.study;

import com.seongje.studyolle.domain.Tag;
import com.seongje.studyolle.domain.account.Account;
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
}
