package com.seongje.studyolle.modules.study.repository.custom;

import com.seongje.studyolle.modules.study.domain.Study;
import com.seongje.studyolle.modules.study.form.StudySearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface StudyRepositoryCustom {

    Page<Study> searchAllByStudySearchAndPaging(StudySearch studySearch, Pageable pageable);
}
