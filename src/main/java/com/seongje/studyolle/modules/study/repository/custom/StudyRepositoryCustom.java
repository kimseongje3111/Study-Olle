package com.seongje.studyolle.modules.study.repository.custom;

import com.seongje.studyolle.modules.study.domain.Study;
import com.seongje.studyolle.modules.study.form.StudySearch;
import com.seongje.studyolle.modules.tag.domain.Tag;
import com.seongje.studyolle.modules.zone.domain.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional(readOnly = true)
public interface StudyRepositoryCustom {

    List<Study> searchPublishedAndNotClosedByTagsAndZonesAndPublishedDateTimeDesc(Set<Tag> tags, Set<Zone> zones);

    Page<Study> searchAllByStudySearchAndPaging(StudySearch studySearch, Pageable pageable);
}
