package com.seongje.studyolle.modules.study.app_event.custom;

import com.seongje.studyolle.modules.study.domain.Study;
import lombok.Getter;

@Getter
public class StudyUpdatedEvent extends StudyAppEvent {

    private StudyUpdatedEventType eventType;
    private String newTitle;
    private String newPath;

    public StudyUpdatedEvent(Study study, StudyUpdatedEventType eventType) {
        super(study, "스터디 변경 사항");
        this.eventType = eventType;
    }

    public void setNewTitle(String newTitle) {
        this.newTitle = newTitle;
    }

    public void setNewPath(String newPath) {
        this.newPath = newPath;
    }
}
