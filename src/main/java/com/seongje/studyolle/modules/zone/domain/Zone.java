package com.seongje.studyolle.modules.zone.domain;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Zone {

    @Id @GeneratedValue
    @Column(name = "zone_id")
    private Long id;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String localNameOfCity;

    private String province;

    @Override
    public String toString() {
        if (province == null) {
            return String.format("%s (%s)", city, localNameOfCity);
        }

        return String.format("%s (%s) / %s", city, localNameOfCity, province);
    }
}
