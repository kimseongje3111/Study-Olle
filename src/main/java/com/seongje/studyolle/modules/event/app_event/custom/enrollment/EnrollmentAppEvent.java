package com.seongje.studyolle.modules.event.app_event.custom.enrollment;

import com.seongje.studyolle.modules.event.domain.Enrollment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter @Setter
@RequiredArgsConstructor
public abstract class EnrollmentAppEvent {

    private final Enrollment enrollment;
    private final String messageTitle;
    private String messageContent;

    public String getMessage() {
        return "[" + messageTitle + "] " + messageContent;
    }
}
