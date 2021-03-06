package com.seongje.studyolle.modules.study.repository;

import com.seongje.studyolle.modules.study.domain.Study;
import com.seongje.studyolle.modules.study.repository.custom.StudyRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long>, StudyRepositoryCustom {

    boolean existsByPath(String path);

    Study findByPath(String path);

    List<Study> findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(boolean published, boolean closed);
}
