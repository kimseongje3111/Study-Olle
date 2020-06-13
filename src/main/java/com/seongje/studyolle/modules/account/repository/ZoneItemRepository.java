package com.seongje.studyolle.modules.account.repository;

import com.seongje.studyolle.modules.account.domain.ZoneItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface ZoneItemRepository extends JpaRepository<ZoneItem, Long> {
}
