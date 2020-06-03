package com.seongje.studyolle.modules.study.repository;

import com.seongje.studyolle.domain.account.Account;
import com.seongje.studyolle.domain.study.Study;
import com.seongje.studyolle.domain.study.StudyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

    void deleteByAccountAndStudy(Account account, Study study);

    boolean existsByAccountAndStudy(Account account, Study study);
}
