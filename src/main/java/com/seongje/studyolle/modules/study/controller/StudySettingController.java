package com.seongje.studyolle.modules.study.controller;

import com.seongje.studyolle.domain.account.Account;
import com.seongje.studyolle.domain.study.Study;
import com.seongje.studyolle.modules.authentication.CurrentUser;
import com.seongje.studyolle.modules.study.StudyService;
import com.seongje.studyolle.modules.study.form.StudyDescriptionsForm;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;

@Controller
@RequiredArgsConstructor
@RequestMapping("/study/studies/{path}/settings")
public class StudySettingController {

    static final String REDIRECT = "redirect:/study/studies/";
    static final String STUDY_SETTINGS = "study/settings";
    static final String DESCRIPTIONS = "/descriptions";
    static final String BANNER = "/banner";
    static final String STUDY_TAGS = "/study-tags";
    static final String STUDY_ZONES = "/study-zones";
    static final String DETAILS = "/details";

    private final StudyService studyService;
    private final ModelMapper modelMapper;

    @GetMapping(DESCRIPTIONS)
    public String studyDescriptionsForm(@CurrentUser Account account,
                                        @PathVariable String path,
                                        Model model) {

        Study findStudy = studyService.findStudyForUpdate(path, account);

        model.addAttribute(account);
        model.addAttribute(findStudy);
        model.addAttribute(modelMapper.map(findStudy, StudyDescriptionsForm.class));

        return STUDY_SETTINGS + DESCRIPTIONS;
    }

    @SneakyThrows
    @PostMapping(DESCRIPTIONS)
    public String studyDescriptionsFormUpdate(@CurrentUser Account account,
                                              @PathVariable String path,
                                              @Valid StudyDescriptionsForm studyDescriptionsForm,
                                              Errors errors,
                                              Model model,
                                              RedirectAttributes attributes) {

        Study findStudy = studyService.findStudyForUpdate(path, account);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(findStudy);
            model.addAttribute(modelMapper.map(findStudy, StudyDescriptionsForm.class));

            return STUDY_SETTINGS + DESCRIPTIONS;
        }

        studyService.updateDescriptions(findStudy, studyDescriptionsForm);
        attributes.addFlashAttribute("message", "수정되었습니다.");

        return REDIRECT + findStudy.getEncodedPath()  + "/settings" + DESCRIPTIONS;
    }

}
