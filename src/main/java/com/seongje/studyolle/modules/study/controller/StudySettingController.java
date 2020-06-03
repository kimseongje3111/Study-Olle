package com.seongje.studyolle.modules.study.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seongje.studyolle.domain.Tag;
import com.seongje.studyolle.domain.Zone;
import com.seongje.studyolle.domain.account.Account;
import com.seongje.studyolle.domain.study.Study;
import com.seongje.studyolle.modules.authentication.CurrentUser;
import com.seongje.studyolle.modules.study.StudyService;
import com.seongje.studyolle.modules.study.form.StudyDescriptionsForm;
import com.seongje.studyolle.modules.tag.TagForm;
import com.seongje.studyolle.modules.tag.TagService;
import com.seongje.studyolle.modules.zone.ZoneForm;
import com.seongje.studyolle.modules.zone.ZoneService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

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
    private final TagService tagService;
    private final ZoneService zoneService;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    @GetMapping(DESCRIPTIONS)
    public String studyDescriptionsForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study findStudy = studyService.findStudyForUpdate(path, account);

        model.addAttribute(account);
        model.addAttribute(findStudy);
        model.addAttribute(modelMapper.map(findStudy, StudyDescriptionsForm.class));

        return STUDY_SETTINGS + DESCRIPTIONS;
    }

    @SneakyThrows
    @PostMapping(DESCRIPTIONS)
    public String studyDescriptionsFormUpdate(@CurrentUser Account account, @PathVariable String path,
                                              @Valid StudyDescriptionsForm studyDescriptionsForm,
                                              Errors errors, Model model, RedirectAttributes attributes) {

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

    @GetMapping(BANNER)
    public String studyBannerForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study findStudy = studyService.findStudyForUpdate(path, account);
        List<String> basicBanners = studyService.getBasicBannerImages();

        model.addAttribute(account);
        model.addAttribute(findStudy);
        model.addAttribute("basic_banners", basicBanners);

        return STUDY_SETTINGS + BANNER;
    }

    @SneakyThrows
    @PostMapping(BANNER)
    public String studyBannerFormUpdate(@CurrentUser Account account, @PathVariable String path,
                                        String studyBannerImage, String basicBanner,
                                        RedirectAttributes attributes) {

        Study findStudy = studyService.findStudyForUpdate(path, account);

        if (studyBannerImage != null) {
            studyService.updateBannerImage(findStudy, studyBannerImage);
        }

        if (basicBanner != null) {
            studyService.updateBannerImageByBasic(findStudy, basicBanner);
        }

        attributes.addFlashAttribute("message", "배너 이미지가 적용되었습니다.");

        return REDIRECT + findStudy.getEncodedPath()  + "/settings" + BANNER;
    }

    @SneakyThrows
    @PostMapping(BANNER + "/enable")
    public String studyBannerImageEnable(@CurrentUser Account account, @PathVariable String path) {
        Study findStudy = studyService.findStudyForUpdate(path, account);

        studyService.useBannerImage(findStudy, true);

        return REDIRECT + findStudy.getEncodedPath()  + "/settings" + BANNER;
    }

    @SneakyThrows
    @PostMapping(BANNER + "/disable")
    public String studyBannerImageDisable(@CurrentUser Account account, @PathVariable String path) {
        Study findStudy = studyService.findStudyForUpdate(path, account);

        studyService.useBannerImage(findStudy, false);

        return REDIRECT + findStudy.getEncodedPath()  + "/settings" + BANNER;
    }

    @GetMapping(STUDY_TAGS)
    public String studyTagsForm(@CurrentUser Account account, @PathVariable String path,
                                Model model) throws JsonProcessingException {

        Study findStudy = studyService.findStudyForUpdate(path, account);
        Set<String> studyTags = studyService.getStudyTags(findStudy);
        List<String> allTags = tagService.getAllTags();

        model.addAttribute(account);
        model.addAttribute(findStudy);
        model.addAttribute("tags", studyTags);
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return STUDY_SETTINGS + STUDY_TAGS;
    }

    @PostMapping(STUDY_TAGS + "/add")
    @ResponseBody
    public ResponseEntity addStudyTags(@CurrentUser Account account, @PathVariable String path,
                                       @RequestBody TagForm tagForm) {

        Tag findOrNewTag = tagService.findOrCreateTag(tagForm.getTagTitle());
        Study findStudy = studyService.findStudyForUpdate(path, account);

        studyService.addStudyTag(findStudy, findOrNewTag);

        return ResponseEntity.ok().build();
    }

    @PostMapping(STUDY_TAGS + "/remove")
    public ResponseEntity removeStudyTags(@CurrentUser Account account, @PathVariable String path,
                                          @RequestBody TagForm tagForm) {

        Tag findTag = tagService.findByTagTitle(tagForm.getTagTitle());
        Study findStudy = studyService.findStudyForUpdate(path, account);

        if (findTag == null) {
            return ResponseEntity.badRequest().build();
        }

        studyService.removeStudyTag(findStudy, findTag);

        return ResponseEntity.ok().build();
    }

    @GetMapping(STUDY_ZONES)
    public String studyZonesForm(@CurrentUser Account account, @PathVariable String path,
                                 Model model) throws JsonProcessingException {

        Study findStudy = studyService.findStudyForUpdate(path, account);
        Set<String> studyZones = studyService.getStudyZones(findStudy);
        List<String> allZones = zoneService.getAllZones();

        model.addAttribute(account);
        model.addAttribute(findStudy);
        model.addAttribute("zones", studyZones);
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));

        return STUDY_SETTINGS + STUDY_ZONES;
    }

    @PostMapping(STUDY_ZONES + "/add")
    @ResponseBody
    public ResponseEntity addStudyZones(@CurrentUser Account account, @PathVariable String path,
                                        @RequestBody ZoneForm zoneForm) {

        Zone findZone = zoneService.findZone(zoneForm);
        Study findStudy = studyService.findStudyForUpdate(path, account);

        studyService.addStudyZone(findStudy, findZone);

        return ResponseEntity.ok().build();
    }

    @PostMapping(STUDY_ZONES + "/remove")
    public ResponseEntity removeStudyZones(@CurrentUser Account account, @PathVariable String path,
                                           @RequestBody ZoneForm zoneForm) {

        Zone findZone = zoneService.findZone(zoneForm);
        Study findStudy = studyService.findStudyForUpdate(path, account);

        if (findZone == null) {
            return ResponseEntity.badRequest().build();
        }

        studyService.removeStudyZone(findStudy, findZone);

        return ResponseEntity.ok().build();
    }

    @GetMapping(DETAILS)
    public String studyDetailsForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study findStudy = studyService.findStudyForUpdate(path, account);

        model.addAttribute(account);
        model.addAttribute(findStudy);

        return STUDY_SETTINGS + DETAILS;
    }

    @SneakyThrows
    @PostMapping(DETAILS + "/publish")
    public String studyPublish(@CurrentUser Account account, @PathVariable String path,
                               RedirectAttributes attributes) {

        Study findStudy = studyService.findStudyForUpdate(path, account);

        studyService.studyPublish(findStudy);
        attributes.addFlashAttribute("message", "스터디를 공개했습니다.");

        return REDIRECT + findStudy.getEncodedPath() + "/settings" + DETAILS;
    }

    @SneakyThrows
    @PostMapping(DETAILS + "/close")
    public String studyClose(@CurrentUser Account account, @PathVariable String path,
                             RedirectAttributes attributes) {

        Study findStudy = studyService.findStudyForUpdate(path, account);

        studyService.studyClose(findStudy);
        attributes.addFlashAttribute("message", "스터디를 종료했습니다.");

        return REDIRECT + findStudy.getEncodedPath() + "/settings" + DETAILS;
    }

    @SneakyThrows
    @PostMapping(DETAILS + "/recruit-start")
    public String studyRecruitStart(@CurrentUser Account account, @PathVariable String path,
                                    RedirectAttributes attributes) {

        Study findStudy = studyService.findStudyForUpdate(path, account);

        if (!findStudy.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "현재는 팀원 모집 상태를 변경할 수 없습니다.");
            return REDIRECT + findStudy.getEncodedPath() + "/settings" + DETAILS;
        }

        studyService.studyRecruitStart(findStudy);
        attributes.addFlashAttribute("message", "팀원 모집을 시작합니다.");

        return REDIRECT + findStudy.getEncodedPath() + "/settings" + DETAILS;
    }

    @SneakyThrows
    @PostMapping(DETAILS + "/recruit-stop")
    public String studyRecruitStop(@CurrentUser Account account, @PathVariable String path,
                                   RedirectAttributes attributes) {

        Study findStudy = studyService.findStudyForUpdate(path, account);

        if (!findStudy.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "현재는 팀원 모집 상태를 변경할 수 없습니다.");
            return REDIRECT + findStudy.getEncodedPath() + "/settings" + DETAILS;
        }

        studyService.studyRecruitStop(findStudy);
        attributes.addFlashAttribute("message", "팀원 모집을 중단합니다.");

        return REDIRECT + findStudy.getEncodedPath() + "/settings" + DETAILS;
    }

    @SneakyThrows
    @PostMapping(DETAILS + "/title")
    public String studyTitleUpdate(@CurrentUser Account account, @PathVariable String path,
                                   String newTitle, Model model, RedirectAttributes attributes) {

        Study findStudy = studyService.findStudyForUpdate(path, account);

        if (!studyService.studyTitleUpdate(findStudy, newTitle)) {
            model.addAttribute(account);
            model.addAttribute(findStudy);
            model.addAttribute("studyTitleError", "스터디 이름이 너무 깁니다.");

            return STUDY_SETTINGS + DETAILS;
        }

        attributes.addFlashAttribute("message", "스터디 이름이 변경되었습니다.");

        return REDIRECT + findStudy.getEncodedPath() + "/settings" + DETAILS;
    }

    @SneakyThrows
    @PostMapping(DETAILS + "/path")
    public String studyPathUpdate(@CurrentUser Account account, @PathVariable String path,
                                  String newPath, Model model, RedirectAttributes attributes) {

        Study findStudy = studyService.findStudyForUpdate(path, account);

        if (!studyService.studyPathUpdate(findStudy, newPath)) {
            model.addAttribute(account);
            model.addAttribute(findStudy);
            model.addAttribute("studyTitleError", "입력 값이 형식에 맞지 않거나 이미 사용 중인 스터디 경로입니다.");

            return STUDY_SETTINGS + DETAILS;
        }

        attributes.addFlashAttribute("message", "스터디 경로가 변경되었습니다.");

        return REDIRECT + findStudy.getEncodedPath() + "/settings" + DETAILS;
    }

    @PostMapping(DETAILS + "/delete")
    public String studyDelete(@CurrentUser Account account, @PathVariable String path) {
        Study findStudy = studyService.findStudyForUpdate(path, account);

        studyService.studyDelete(findStudy);

        return "redirect:/";
    }
}
