package com.seongje.studyolle.modules.study;

import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.study.domain.Study;
import com.seongje.studyolle.modules.study.repository.StudyRepository;
import com.seongje.studyolle.modules.study.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudyFactory {

    @Autowired StudyService studyService;
    @Autowired StudyRepository studyRepository;

    public Study createStudy(String path, Account manager) {
        Study study = new Study();

        study.setPath(path);
        studyService.createNewStudy(study, manager);

        return study;
    }
}
