package com.seongje.studyolle.modules.study.domain;

import com.seongje.studyolle.modules.tag.domain.Tag;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "study_tag_item")
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class StudyTagItem {

    @Id @GeneratedValue
    @Column(name = "study_tag_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    // 생성 메서드 //

    public static StudyTagItem createStudyTagItem(Study study, Tag tag) {
        return StudyTagItem.builder().study(study).tag(tag).build();
    }
}
