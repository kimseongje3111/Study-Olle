package com.seongje.studyolle.modules.study.app_event.custom;

import com.seongje.studyolle.modules.study.domain.Study;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter @Setter
@RequiredArgsConstructor
public abstract class StudyAppEvent {

    private final Study study;
    private final String messageTitle;
    private String messageContent;

    public String getMessage() {
        return "[" + messageTitle + "] " + messageContent;
    }
}
