package com.seongje.studyolle.modules.event.validator;

import com.seongje.studyolle.domain.event.Event;
import com.seongje.studyolle.modules.event.form.EventForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
public class EventFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(EventForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        EventForm eventForm = (EventForm) target;

        if (!isValidStartDateTime(eventForm)) {
            errors.rejectValue("startDateTime", "wrong.datetime", "모임 시작 일시를 정확히 입력하세요.");
        }

        if (!isValidEndDateTime(eventForm)) {
            errors.rejectValue("endDateTime", "wrong.datetime", "모임 종료 일시를 정확히 입력하세요.");
        }

        if (!isValidEnrollmentDeadLine(eventForm)) {
            errors.rejectValue("enrollmentDeadline", "wrong.datetime", "등록 마감 일시를 정확히 입력하세요.");
        }
    }

    public void validateEventFormForUpdate(EventForm eventForm, Event event, Errors errors) {
        if (eventForm.getLimitOfEnrollments() < event.getNumberOfAcceptedEnrollments()) {
            errors.rejectValue("limitOfEnrollments", "wrong.value", "모집 인원은 이미 확정된 참가 신청 수보다 커야 합니다.");
        }
    }

    private boolean isValidStartDateTime(EventForm eventForm) {
        LocalDateTime startDateTime = eventForm.getStartDateTime();

        return startDateTime.isBefore(eventForm.getEndDateTime())
                && startDateTime.isAfter(eventForm.getEnrollmentDeadline())
                && startDateTime.isAfter(LocalDateTime.now());
    }

    private boolean isValidEndDateTime(EventForm eventForm) {
        LocalDateTime endDateTime = eventForm.getEndDateTime();

        return endDateTime.isAfter(eventForm.getStartDateTime())
                && endDateTime.isAfter(eventForm.getEnrollmentDeadline())
                && endDateTime.isAfter(LocalDateTime.now());
    }

    private boolean isValidEnrollmentDeadLine(EventForm eventForm) {
        LocalDateTime enrollmentDeadline = eventForm.getEnrollmentDeadline();

        return enrollmentDeadline.isBefore(eventForm.getStartDateTime())
                && enrollmentDeadline.isBefore(eventForm.getEndDateTime())
                && enrollmentDeadline.isAfter(LocalDateTime.now());
    }
}
