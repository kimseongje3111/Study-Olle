package com.seongje.studyolle.modules.study.app_event.custom;

import com.seongje.studyolle.modules.study.domain.Study;
import lombok.Getter;

@Getter
public class StudyUpdatedEvent extends StudyAppEvent {

    private StudyUpdatedEventType eventType;
    private String prevTitle;
    private String prevPath;

    public StudyUpdatedEvent(Study study, StudyUpdatedEventType eventType) {
        super(study, "스터디 변경 사항");
        this.eventType = eventType;
    }

    public void setPrevTitle(String prevTitle) {
        this.prevTitle = prevTitle;
    }

    public void setPrevPath(String prevPath) {
        this.prevPath = prevPath;
    }
}
