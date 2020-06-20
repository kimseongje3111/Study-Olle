package com.seongje.studyolle.modules.study.repository.custom;


import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seongje.studyolle.modules.account.domain.QAccount;
import com.seongje.studyolle.modules.study.domain.QStudy;
import com.seongje.studyolle.modules.study.domain.QStudyMember;
import com.seongje.studyolle.modules.study.domain.StudyMember;

import javax.persistence.EntityManager;
import java.util.List;

import static com.seongje.studyolle.modules.account.domain.QAccount.*;
import static com.seongje.studyolle.modules.study.domain.QStudy.*;
import static com.seongje.studyolle.modules.study.domain.QStudyMember.*;

public class StudyMemberRepositoryImpl implements StudyMemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public StudyMemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<StudyMember> searchAllByAccountAndPublishedDateTimeDesc(Long accountId) {
        return queryFactory
                .selectFrom(studyMember)
                .join(studyMember.study, study).fetchJoin()
                .join(studyMember.account, account).fetchJoin()
                .where(account.id.eq(accountId))
                .orderBy(study.publishedDateTime.desc())
                .fetch();
    }
}
