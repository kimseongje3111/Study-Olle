package com.seongje.studyolle.modules.event.controller;

import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.event.domain.Event;
import com.seongje.studyolle.modules.study.domain.Study;
import com.seongje.studyolle.modules.account.authentication.CurrentUser;
import com.seongje.studyolle.modules.event.service.EventService;
import com.seongje.studyolle.modules.event.form.EventForm;
import com.seongje.studyolle.modules.event.validator.EventFormValidator;
import com.seongje.studyolle.modules.study.service.StudyService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/study/studies/{path}/")
public class EventController {

    static final String REDIRECT = "redirect:/study/studies/";
    static final String NEW_EVENT = "event/new-event";
    static final String EVENTS = "event/events";
    static final String EVENT_HOME = EVENTS + "/{eventId}";
    static final String EVENT_UPDATE = EVENT_HOME + "/edit";
    static final String EVENT_DELETE = EVENT_HOME + "/delete";
    static final String EVENT_ENROLL = EVENT_HOME + "/enroll";
    static final String EVENT_ENROLL_CANCEL = EVENT_HOME + "/disenroll";

    private final EventService eventService;
    private final StudyService studyService;
    private final EventFormValidator eventFormValidator;
    private final ModelMapper modelMapper;

    @InitBinder("eventForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(eventFormValidator);
    }

    @GetMapping(NEW_EVENT)
    public String newEventForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study findStudy = studyService.findStudyForUpdate(path, account);

        model.addAttribute(account);
        model.addAttribute(findStudy);
        model.addAttribute(new EventForm());

        return NEW_EVENT;
    }

    @SneakyThrows
    @PostMapping(NEW_EVENT)
    public String newEventFormSubmit(@CurrentUser Account account, @PathVariable String path,
                                     @Valid EventForm eventForm, Errors errors, Model model) {

        Study findStudy = studyService.findStudyForUpdate(path, account);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(findStudy);

            return NEW_EVENT;
        }

        Event newEvent = eventService.createNewEvent(modelMapper.map(eventForm, Event.class), account, findStudy);
        eventService.enrollToEvent(newEvent, account);

        return REDIRECT + findStudy.getEncodedPath() + "/event/events/" + newEvent.getId();
    }

    @GetMapping(EVENTS)
    public String studyEvents(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study findStudy = studyService.findStudy(path);

        List<Event> newEvents = eventService.getEventsFor(findStudy.getId(), "NEW");
        List<Event> nowEvents = eventService.getEventsFor(findStudy.getId(), "NOW");
        List<Event> oldEvents = eventService.getEventsFor(findStudy.getId(), "OLD");

        // TODO : 페이징

        model.addAttribute(account);
        model.addAttribute(findStudy);
        model.addAttribute("newEvents", newEvents);
        model.addAttribute("nowEvents", nowEvents);
        model.addAttribute("oldEvents", oldEvents);

        return "study/study-events";
    }

    @GetMapping(EVENT_HOME)
    public String eventHome(@CurrentUser Account account, @PathVariable String path,
                            @PathVariable Long eventId, Model model) {

        Study findStudy = studyService.findStudy(path);
        Event findEvent = eventService.findEvent(eventId);

        model.addAttribute(account);
        model.addAttribute(findStudy);
        model.addAttribute(findEvent);

        return "event/event-home";
    }

    @GetMapping(EVENT_UPDATE)
    public String eventUpdateForm(@CurrentUser Account account, @PathVariable String path,
                                  @PathVariable Long eventId, Model model) {

        Study findStudy = studyService.findStudyForUpdate(path, account);
        Event findEvent = eventService.findEvent(eventId);

        model.addAttribute(account);
        model.addAttribute(findStudy);
        model.addAttribute(findEvent);
        model.addAttribute(modelMapper.map(findEvent, EventForm.class));

        return "event/update-event";
    }

    @SneakyThrows
    @PostMapping(EVENT_UPDATE)
    public String eventUpdateFormSubmit(@CurrentUser Account account, @PathVariable String path,
                                        @PathVariable Long eventId, @Valid EventForm eventForm,
                                        Errors errors, Model model) {

        Study findStudy = studyService.findStudyForUpdate(path, account);
        Event findEvent = eventService.findEvent(eventId);

        eventForm.setEventType(findEvent.getEventType());
        eventFormValidator.validateEventFormForUpdate(eventForm, findEvent, errors);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(findStudy);
            model.addAttribute(findEvent);

            return "event/update-event";
        }

        eventService.updateEvent(findEvent, eventForm);

        return REDIRECT + findStudy.getEncodedPath() + "/event/events/" + findEvent.getId();
    }

    @SneakyThrows
    @PostMapping(EVENT_DELETE)
    public String eventDelete(@CurrentUser Account account, @PathVariable String path,
                              @PathVariable Long eventId) {

        Study findStudy = studyService.findStudyForUpdate(path, account);
        Event findEvent = eventService.findEvent(eventId);

        eventService.deleteEvent(findEvent);

        return REDIRECT + findStudy.getEncodedPath() + "/event/events";
    }

    @SneakyThrows
    @PostMapping(EVENT_ENROLL)
    public String eventEnroll(@CurrentUser Account account, @PathVariable String path,
                              @PathVariable Long eventId) {

        Study findStudy = studyService.findStudy(path);
        Event findEvent = eventService.findEvent(eventId);

        eventService.enrollToEvent(findEvent, account);

        return REDIRECT + findStudy.getEncodedPath() + "/event/events/" + findEvent.getId();
    }

    @SneakyThrows
    @PostMapping(EVENT_ENROLL_CANCEL)
    public String eventEnrollCancel(@CurrentUser Account account, @PathVariable String path,
                                    @PathVariable Long eventId) {

        Study findStudy = studyService.findStudy(path);
        Event findEvent = eventService.findEvent(eventId);

        eventService.cancelEnrollFromEvent(findEvent, account);

        return REDIRECT + findStudy.getEncodedPath() + "/event/events/" + findEvent.getId();
    }
}
