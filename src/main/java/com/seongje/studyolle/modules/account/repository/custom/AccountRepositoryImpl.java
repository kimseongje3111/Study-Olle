package com.seongje.studyolle.modules.account.repository.custom;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.tag.domain.Tag;
import com.seongje.studyolle.modules.zone.domain.Zone;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Set;

import static com.seongje.studyolle.modules.account.domain.QAccount.account;

public class AccountRepositoryImpl implements AccountRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public AccountRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Account> searchAllByTagsAndZones(Set<Tag> tags, Set<Zone> zones) {
        return queryFactory
                .selectFrom(account)
                .distinct()
                .join(account.tags)
                .join(account.zones)
                .where(
                        account.tags.any().tag.in(tags),
                        account.zones.any().zone.in(zones)
                )
                .fetch();
    }
}
