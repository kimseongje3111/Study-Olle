package com.seongje.studyolle.modules.study.repository;

import com.seongje.studyolle.domain.study.StudyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {
}
