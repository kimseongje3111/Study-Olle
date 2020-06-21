package com.seongje.studyolle.modules.study.repository;

import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.study.domain.Study;
import com.seongje.studyolle.modules.study.domain.StudyMember;
import com.seongje.studyolle.modules.study.repository.custom.StudyMemberRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface StudyMemberRepository extends JpaRepository<StudyMember, Long>, StudyMemberRepositoryCustom {

    boolean existsByAccountAndStudy(Account account, Study study);

    @Transactional
    void deleteByAccountAndStudy(Account account, Study study);
}
