package com.seongje.studyolle.modules.study.app_event.custom;

import com.seongje.studyolle.modules.study.domain.Study;

public class StudyDeletedEvent extends StudyAppEvent {

    public StudyDeletedEvent(Study study) {
        super(study, "스터디 삭제");
    }
}
