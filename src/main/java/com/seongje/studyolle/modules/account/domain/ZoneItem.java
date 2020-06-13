package com.seongje.studyolle.modules.account.domain;

import com.seongje.studyolle.modules.zone.domain.Zone;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "zone_item")
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class ZoneItem {

    @Id @GeneratedValue
    @Column(name = "zone_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    // 생성 메서드 //

    public static ZoneItem createZoneItem(Account account, Zone zone) {
        return ZoneItem.builder().account(account).zone(zone).build();
    }
}
