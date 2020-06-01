package com.seongje.studyolle.modules.study.repository;

import com.seongje.studyolle.domain.study.StudyTagItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyTagItemRepository extends JpaRepository<StudyTagItem, Long> {
}
