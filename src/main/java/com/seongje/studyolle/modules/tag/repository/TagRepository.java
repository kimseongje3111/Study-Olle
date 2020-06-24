package com.seongje.studyolle.modules.tag.repository;

import com.seongje.studyolle.modules.tag.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface TagRepository extends JpaRepository<Tag, Long> {

    Tag findByTitle(String title);
}
