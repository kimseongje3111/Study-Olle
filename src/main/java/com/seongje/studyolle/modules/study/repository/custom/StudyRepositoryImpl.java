package com.seongje.studyolle.modules.study.repository.custom;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seongje.studyolle.infra.querydsl.Querydsl4RepositorySupport;
import com.seongje.studyolle.modules.study.domain.Study;
import com.seongje.studyolle.modules.study.form.StudySearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;

import java.util.List;

import static com.seongje.studyolle.modules.study.domain.QStudy.*;

public class StudyRepositoryImpl extends Querydsl4RepositorySupport implements StudyRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public StudyRepositoryImpl(EntityManager em) {
        super(Study.class);
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Study> searchAllByStudySearchAndPaging(StudySearch studySearch, Pageable pageable) {
        List<Study> findStudies = queryFactory
                .selectFrom(study)
                .distinct()
                .leftJoin(study.tags)
                .leftJoin(study.zones)
                .where(
                        study.published.isTrue(),
                        study.closed.eq(studySearch.isClosed()),
                        keywordContainsInTitleOrTagsOrZones(studySearch.getKeyword())
                )
                .fetch();

        return applyPagination(pageable,
                contentQuery -> contentQuery.selectFrom(study).where(study.in(findStudies)));
    }

    private BooleanExpression keywordContainsInTitleOrTagsOrZones(String keyword) {
        return study.title.containsIgnoreCase(keyword)
                .or(study.tags.any().tag.title.containsIgnoreCase(keyword))
                .or(study.zones.any().zone.localNameOfCity.containsIgnoreCase(keyword));
    }
}
