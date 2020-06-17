package com.seongje.studyolle.modules.event.app_event.custom.enrollment;

import com.seongje.studyolle.modules.event.domain.Enrollment;

public class EnrollmentScheduledEvent extends EnrollmentAppEvent {

    public EnrollmentScheduledEvent(Enrollment enrollment) {
        super(enrollment, "모임 일정");
    }
}
