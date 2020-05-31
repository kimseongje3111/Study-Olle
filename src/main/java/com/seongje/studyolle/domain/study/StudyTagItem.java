package com.seongje.studyolle.domain.study;

import com.seongje.studyolle.domain.Tag;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "study_tag_item")
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class StudyTagItem {

    @Id @GeneratedValue
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