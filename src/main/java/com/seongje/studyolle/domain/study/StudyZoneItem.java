package com.seongje.studyolle.domain.study;

import com.seongje.studyolle.domain.Zone;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "study_zone_item")
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class StudyZoneItem {

    @Id @GeneratedValue
    @Column(name = "study_zone_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    // 생성 메서드 //

    public static StudyZoneItem createStudyZoneItem(Study study, Zone zone) {
        return StudyZoneItem.builder().study(study).zone(zone).build();
    }
}
