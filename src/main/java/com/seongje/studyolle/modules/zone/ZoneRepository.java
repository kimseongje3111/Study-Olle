package com.seongje.studyolle.modules.zone;

import com.seongje.studyolle.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {

    Zone findByCityAndLocalNameOfCity(String city, String localNameCity);
}
