package com.seongje.studyolle.modules.account.repository;

import com.seongje.studyolle.domain.account.TagItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagItemRepository extends JpaRepository<TagItem, Long> {
}
