package com.seongje.studyolle.modules.study.repository;

import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.study.domain.Study;
import com.seongje.studyolle.modules.study.domain.StudyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

    @Transactional
    void deleteByAccountAndStudy(Account account, Study study);

    boolean existsByAccountAndStudy(Account account, Study study);
}
