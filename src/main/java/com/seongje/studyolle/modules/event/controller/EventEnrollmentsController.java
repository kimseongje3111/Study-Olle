package com.seongje.studyolle.modules.event.controller;

import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.event.domain.Enrollment;
import com.seongje.studyolle.modules.event.domain.Event;
import com.seongje.studyolle.modules.study.domain.Study;
import com.seongje.studyolle.modules.account.authentication.CurrentUser;
import com.seongje.studyolle.modules.event.service.EventService;
import com.seongje.studyolle.modules.study.service.StudyService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/study/studies/{path}/event/events/{eventId}/enrollments/{enrollmentId}")
public class EventEnrollmentsController {

    static final String REDIRECT = "redirect:/study/studies/";
    static final String ENROLLMENT_ACCEPT = "/accept";
    static final String ENROLLMENT_REJECT = "/reject";
    static final String ENROLLMENT_CHECKIN = "/checkin";
    static final String ENROLLMENT_CHECKIN_CANCEL = "/checkin-cancel";

    private final EventService eventService;
    private final StudyService studyService;

    @SneakyThrows
    @GetMapping(ENROLLMENT_ACCEPT)
    public String enrollmentAcceptFromEventByManagerConfirm(@CurrentUser Account account,
                                                            @PathVariable String path,
                                                            @PathVariable Long eventId,
                                                            @PathVariable Long enrollmentId) {

        Study findStudy = studyService.findStudyForUpdate(path, account);
        Event findEvent = eventService.findEvent(eventId);
        Enrollment findEnrollment = eventService.findEnrollment(enrollmentId);

        eventService.enrollmentAccept(findEvent, findEnrollment);

        return REDIRECT + findStudy.getEncodedPath() + "/event/events/" + findEvent.getId();
    }

    @SneakyThrows
    @GetMapping(ENROLLMENT_REJECT)
    public String enrollmentRejectFromEventByManagerConfirm(@CurrentUser Account account,
                                                            @PathVariable String path,
                                                            @PathVariable Long eventId,
                                                            @PathVariable Long enrollmentId) {

        Study findStudy = studyService.findStudyForUpdate(path, account);
        Event findEvent = eventService.findEvent(eventId);
        Enrollment findEnrollment = eventService.findEnrollment(enrollmentId);

        eventService.enrollmentReject(findEvent, findEnrollment);

        return REDIRECT + findStudy.getEncodedPath() + "/event/events/" + findEvent.getId();
    }

    @SneakyThrows
    @GetMapping(ENROLLMENT_CHECKIN)
    public String enrollmentCheckIn(@CurrentUser Account account, @PathVariable String path,
                                    @PathVariable Long eventId, @PathVariable Long enrollmentId) {

        Study findStudy = studyService.findStudyForUpdate(path, account);
        Event findEvent = eventService.findEvent(eventId);
        Enrollment findEnrollment = eventService.findEnrollment(enrollmentId);

        eventService.enrollmentCheckIn(findEvent, findEnrollment);

        return REDIRECT + findStudy.getEncodedPath() + "/event/events/" + findEvent.getId();
    }

    @SneakyThrows
    @GetMapping(ENROLLMENT_CHECKIN_CANCEL)
    public String enrollmentCheckInCancel(@CurrentUser Account account, @PathVariable String path,
                                          @PathVariable Long eventId, @PathVariable Long enrollmentId) {

        Study findStudy = studyService.findStudyForUpdate(path, account);
        Event findEvent = eventService.findEvent(eventId);
        Enrollment findEnrollment = eventService.findEnrollment(enrollmentId);

        eventService.enrollmentCheckInCancel(findEvent, findEnrollment);

        return REDIRECT + findStudy.getEncodedPath() + "/event/events/" + findEvent.getId();
    }
}
