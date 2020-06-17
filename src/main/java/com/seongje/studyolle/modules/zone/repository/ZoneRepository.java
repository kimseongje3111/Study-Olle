package com.seongje.studyolle.modules.zone.repository;

import com.seongje.studyolle.modules.zone.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface ZoneRepository extends JpaRepository<Zone, Long> {

    Zone findByCityAndLocalNameOfCity(String city, String localNameCity);
}
