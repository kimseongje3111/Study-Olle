package com.seongje.studyolle.modules.account.repository.custom;

import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.tag.domain.Tag;
import com.seongje.studyolle.modules.zone.domain.Zone;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional(readOnly = true)
public interface AccountRepositoryCustom {

    List<Account> searchAllByTagsAndZones(Set<Tag> tags, Set<Zone> zones);
}
