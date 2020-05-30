package com.seongje.studyolle.modules.study.controller;

import com.seongje.studyolle.domain.account.Account;
import com.seongje.studyolle.domain.study.Study;
import com.seongje.studyolle.modules.authentication.CurrentUser;
import com.seongje.studyolle.modules.study.StudyService;
import com.seongje.studyolle.modules.study.form.StudyForm;
import com.seongje.studyolle.modules.study.validator.StudyFormValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class StudyController {

    static final String REDIRECT = "redirect:/";
    static final String NEW_STUDY = "study/new-study";
    static final String STUDIES = "study/studies";
    static final String STUDY_HOME = STUDIES + "/{path}";
    static final String STUDY_MEMBERS = STUDY_HOME + "/members";

    private final StudyService studyService;
    private final StudyFormValidator studyFormValidator;
    private final ModelMapper modelMapper;

    @InitBinder("studyForm")
    public void studyFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(studyFormValidator);
    }

    @GetMapping(NEW_STUDY)
    public String newStudyForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new StudyForm());

        return NEW_STUDY;
    }

    @PostMapping(NEW_STUDY)
    public String newStudySubmit(@CurrentUser Account account,
                                 @Valid StudyForm studyForm,
                                 Errors errors,
                                 Model model) throws UnsupportedEncodingException {

        if (errors.hasErrors()) {
            model.addAttribute(account);
            return NEW_STUDY;
        }

        Study newStudy = studyService.createNewStudy(modelMapper.map(studyForm, Study.class), account);

        return REDIRECT + STUDIES + "/" + URLEncoder.encode(newStudy.getPath(), "UTF-8");
    }

    @GetMapping(STUDY_HOME)
    public String studyHome(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study findStudy = studyService.findStudy(path);

        model.addAttribute(account);
        model.addAttribute(findStudy);

        return "study/study-home";
    }

    @GetMapping(STUDY_MEMBERS)
    public String studyMembers(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study findStudy = studyService.findStudy(path);

        model.addAttribute(account);
        model.addAttribute(findStudy);

        return "study/study-members";
    }
}
