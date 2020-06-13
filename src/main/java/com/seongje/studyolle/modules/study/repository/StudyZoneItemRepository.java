package com.seongje.studyolle.modules.study.repository;

import com.seongje.studyolle.modules.study.domain.StudyZoneItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface StudyZoneItemRepository extends JpaRepository<StudyZoneItem, Long> {
}
