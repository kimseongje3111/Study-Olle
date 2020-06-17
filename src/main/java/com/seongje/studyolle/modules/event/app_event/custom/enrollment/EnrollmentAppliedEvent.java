package com.seongje.studyolle.modules.event.app_event.custom.enrollment;

import com.seongje.studyolle.modules.event.domain.Enrollment;

public class EnrollmentAppliedEvent extends EnrollmentAppEvent {

    public EnrollmentAppliedEvent(Enrollment enrollment) {
        super(enrollment, "모임 신청/결과");
    }
}
