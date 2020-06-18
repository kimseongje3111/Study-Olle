package com.seongje.studyolle.modules.study.repository.custom;

import com.seongje.studyolle.modules.study.domain.StudyMember;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface StudyMemberRepositoryCustom {

    List<StudyMember> searchAllByAccount(Long accountId);
}
