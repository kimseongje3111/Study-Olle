package com.seongje.studyolle.modules.event.app_event.custom.enrollment;

import com.seongje.studyolle.modules.event.domain.Enrollment;
import lombok.Getter;

@Getter
public class EnrollmentAppliedEvent extends EnrollmentAppEvent {

    private EnrollmentResultType eventType;

    public EnrollmentAppliedEvent(Enrollment enrollment, EnrollmentResultType eventType) {
        super(enrollment, "모임 신청/결과");
        this.eventType = eventType;
    }
}
