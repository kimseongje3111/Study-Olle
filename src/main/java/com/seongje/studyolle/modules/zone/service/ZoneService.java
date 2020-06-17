package com.seongje.studyolle.modules.zone.service;

import com.seongje.studyolle.modules.zone.form.ZoneForm;
import com.seongje.studyolle.modules.zone.repository.ZoneRepository;
import com.seongje.studyolle.modules.zone.domain.Zone;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import static com.seongje.studyolle.modules.zone.domain.Zone.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ZoneService {

    private final ZoneRepository zoneRepository;

    @PostConstruct
    public void initZones() throws IOException {
        if (zoneRepository.count() == 0) {
            Resource resource = new ClassPathResource("zones_kr.csv");

            List<Zone> zoneList = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8)
                    .stream()
                    .map(line -> {
                        String[] value = line.split(",");
                        ZoneBuilder zoneBuilder = builder().city(value[0]).localNameOfCity(value[1]);

                        if (!value[2].equals("none")) {
                            zoneBuilder.province(value[2]);
                        }

                        return zoneBuilder.build();
                    })
                    .collect(Collectors.toList());

            zoneRepository.saveAll(zoneList);
        }
    }

    public Zone findZone(ZoneForm zoneForm) {
        return zoneRepository.findByCityAndLocalNameOfCity(zoneForm.getCityName(), zoneForm.getLocalNameOfCity());
    }

    public List<String> getAllZones() {
        return zoneRepository.findAll().stream()
                .map(Zone::toString)
                .sorted()
                .collect(Collectors.toList());
    }
}
