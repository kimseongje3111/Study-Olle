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

    static final String STUDY_PATH = "study/";
    static final String NEW_STUDY = "new-study";
    static final String STUDY_URL = "study/{path}";

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

        return STUDY_PATH + NEW_STUDY;
    }

    @PostMapping(NEW_STUDY)
    public String newStudySubmit(@CurrentUser Account account,
                                 @Valid StudyForm studyForm,
                                 Errors errors,
                                 Model model) throws UnsupportedEncodingException {

        if (errors.hasErrors()) {
            model.addAttribute(account);
            return STUDY_PATH + NEW_STUDY;
        }

        Study newStudy = studyService.createNewStudy(modelMapper.map(studyForm, Study.class), account);

        return "redirect:/" + STUDY_PATH + URLEncoder.encode(newStudy.getPath(), "UTF-8");
    }

    @GetMapping(STUDY_URL)
    public String studyHome(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study findStudy = studyService.findStudy(path);

        model.addAttribute(account);
        model.addAttribute(findStudy);

        return STUDY_PATH + "study-home";
    }

    @GetMapping(STUDY_URL + "/members")
    public String studyMembers(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study findStudy = studyService.findStudy(path);

        model.addAttribute(account);
        model.addAttribute(findStudy);

        return STUDY_PATH + "study-members";
    }
}
