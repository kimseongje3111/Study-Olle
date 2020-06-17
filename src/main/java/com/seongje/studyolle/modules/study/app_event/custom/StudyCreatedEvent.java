package com.seongje.studyolle.modules.study.app_event.custom;

import com.seongje.studyolle.modules.study.domain.Study;

public class StudyCreatedEvent extends StudyAppEvent {

    public StudyCreatedEvent(Study study) {
        super(study, "새 스터디 오픈");
    }
}
