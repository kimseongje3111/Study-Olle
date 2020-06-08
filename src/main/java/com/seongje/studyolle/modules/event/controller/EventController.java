package com.seongje.studyolle.modules.event.controller;

import com.seongje.studyolle.domain.account.Account;
import com.seongje.studyolle.domain.event.Event;
import com.seongje.studyolle.domain.study.Study;
import com.seongje.studyolle.modules.authentication.CurrentUser;
import com.seongje.studyolle.modules.event.EventService;
import com.seongje.studyolle.modules.event.form.EventForm;
import com.seongje.studyolle.modules.event.validator.EventFormValidator;
import com.seongje.studyolle.modules.study.StudyService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@RequestMapping("/study/studies/{path}/")
public class EventController {

    static final String REDIRECT = "redirect:/study/studies/";
    static final String NEW_EVENT = "event/new-event";
    static final String EVENT_HOME =  "event/events/{id}";
    static final String EVENT_UPDATE = EVENT_HOME + "/edit";

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

    @GetMapping(EVENT_HOME)
    public String eventHome(@CurrentUser Account account, @PathVariable String path,
                            @PathVariable Long id, Model model) {

        Study findStudy = studyService.findStudy(path);
        Event findEvent = eventService.findEvent(id);

        model.addAttribute(account);
        model.addAttribute(findStudy);
        model.addAttribute(findEvent);

        return "event/view";
    }

    @GetMapping(EVENT_UPDATE)
    public String eventUpdateForm(@CurrentUser Account account, @PathVariable String path,
                                  @PathVariable Long id, Model model) {

        Study findStudy = studyService.findStudyForUpdate(path, account);
        Event findEvent = eventService.findEvent(id);

        model.addAttribute(account);
        model.addAttribute(findStudy);
        model.addAttribute(findEvent);
        model.addAttribute(modelMapper.map(findEvent, EventForm.class));

        return "event/update-view";
    }

    @SneakyThrows
    @PostMapping(EVENT_UPDATE)
    public String eventUpdateFormSubmit(@CurrentUser Account account, @PathVariable String path,
                                        @PathVariable Long id, @Valid EventForm eventForm,
                                        Errors errors, Model model) {

        Study findStudy = studyService.findStudyForUpdate(path, account);
        Event findEvent = eventService.findEvent(id);

        eventForm.setEventType(findEvent.getEventType());
        eventFormValidator.validateEventFormForUpdate(eventForm, findEvent, errors);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(findStudy);
            model.addAttribute(findEvent);

            return "event/update-view";
        }

        eventService.updateEvent(findEvent, eventForm);

        return REDIRECT + findStudy.getEncodedPath() + "/event/events/" + findEvent.getId();
    }


}
