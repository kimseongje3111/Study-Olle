package com.seongje.studyolle.modules.account.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.tag.domain.Tag;
import com.seongje.studyolle.modules.zone.domain.Zone;
import com.seongje.studyolle.modules.account.service.AccountService;
import com.seongje.studyolle.modules.account.authentication.CurrentUser;
import com.seongje.studyolle.modules.account.form.AccountForm;
import com.seongje.studyolle.modules.account.form.NotificationsForm;
import com.seongje.studyolle.modules.account.form.PasswordForm;
import com.seongje.studyolle.modules.account.form.ProfileForm;
import com.seongje.studyolle.modules.account.validator.AccountFormValidator;
import com.seongje.studyolle.modules.account.validator.PasswordFormValidator;
import com.seongje.studyolle.modules.tag.form.TagForm;
import com.seongje.studyolle.modules.tag.service.TagService;
import com.seongje.studyolle.modules.zone.form.ZoneForm;
import com.seongje.studyolle.modules.zone.service.ZoneService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class AccountSettingController {

    static final String REDIRECT= "redirect:/";
    static final String SETTINGS = "account/settings";
    static final String PROFILE = SETTINGS + "/profile";
    static final String PASSWORD = SETTINGS + "/password";
    static final String NOTIFICATIONS = SETTINGS + "/notifications";
    static final String TAGS = SETTINGS + "/tags";
    static final String ZONES = SETTINGS + "/zones";
    static final String ACCOUNT = SETTINGS + "/account";

    private final AccountService accountService;
    private final TagService tagService;
    private final ZoneService zoneService;
    private final PasswordFormValidator passwordFormValidator;
    private final AccountFormValidator accountFormValidator;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    @InitBinder("passwordForm")
    public void initBinderOfPasswordForm(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(passwordFormValidator);
    }

    @InitBinder("accountForm")
    public void initBinderOfAccountForm(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(accountFormValidator);
    }

    @GetMapping(PROFILE)
    public String updateProfileForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, ProfileForm.class));

        return PROFILE;
    }

    @PostMapping(PROFILE)
    public String updateProfileFormSubmit(@CurrentUser Account account, @Valid ProfileForm profileForm,
                                          Errors errors, Model model, RedirectAttributes attributes) {

        if (errors.hasErrors()) {
            model.addAttribute(account);
            return PROFILE;
        }

        accountService.updateProfile(account, profileForm);
        attributes.addFlashAttribute("message", "프로필이 수정되었습니다.");

        return REDIRECT + PROFILE;
    }

    @GetMapping(PASSWORD)
    public String updatePasswordForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());

        return PASSWORD;
    }

    @PostMapping(PASSWORD)
    public String updatePasswordFormSubmit(@CurrentUser Account account, @Valid PasswordForm passwordForm,
                                           Errors errors, Model model, RedirectAttributes attributes) {

        if (errors.hasErrors()) {
            model.addAttribute(account);
            return PASSWORD;
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "패스워드가 변경되었습니다.");

        return REDIRECT + PASSWORD;
    }

    @GetMapping(NOTIFICATIONS)
    public String updatedNotificationsForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NotificationsForm.class));

        return NOTIFICATIONS;
    }

    @PostMapping(NOTIFICATIONS)
    public String updatedNotificationsFormSubmit(@CurrentUser Account account, @Valid NotificationsForm notificationsForm,
                                                 Errors errors, Model model, RedirectAttributes attributes) {

        if (errors.hasErrors()) {
            model.addAttribute(account);
            return NOTIFICATIONS;
        }

        accountService.updateNotifications(account, notificationsForm);
        attributes.addFlashAttribute("message", "변경 사항이 저장되었습니다.");

        return REDIRECT + NOTIFICATIONS;
    }

    @GetMapping(TAGS)
    public String updateTagForm(@CurrentUser Account account, Model model) throws JsonProcessingException {
        List<String> allTags = tagService.getAllTags();
        Set<String> userTags = accountService.getUserTags(account);

        model.addAttribute(account);
        model.addAttribute("tags", userTags);
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return TAGS;
    }

    @PostMapping(TAGS + "/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentUser Account account, @RequestBody TagForm tagForm) {
        Tag findOrNewTag = tagService.findOrCreateTag(tagForm.getTagTitle());

        accountService.addTag(account, findOrNewTag);

        return ResponseEntity.ok().build();
    }

    @PostMapping(TAGS + "/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentUser Account account, @RequestBody TagForm tagForm) {
        Tag findTag = tagService.findByTagTitle(tagForm.getTagTitle());

        if (findTag == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.removeTag(account, findTag);

        return ResponseEntity.ok().build();
    }

    @GetMapping(ZONES)
    public String updateZoneForm(@CurrentUser Account account, Model model) throws JsonProcessingException {
        List<String> allZones = zoneService.getAllZones();
        Set<String> userZones = accountService.getUserZones(account);

        model.addAttribute(account);
        model.addAttribute("zones", userZones);
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));

        return ZONES;
    }

    @PostMapping(ZONES + "/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentUser Account account, @RequestBody ZoneForm zoneForm) {
        Zone findZone = zoneService.findZone(zoneForm);

        if (findZone == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.addZone(account, findZone);

        return ResponseEntity.ok().build();
    }

    @PostMapping(ZONES + "/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentUser Account account, @RequestBody ZoneForm zoneForm) {
        Zone findZone = zoneService.findZone(zoneForm);

        if (findZone == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.removeZone(account, findZone);

        return ResponseEntity.ok().build();
    }

    @GetMapping(ACCOUNT)
    public String updateAccountForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, AccountForm.class));

        return ACCOUNT;
    }

    @PostMapping(ACCOUNT)
    public String updateAccountFormSubmit(@CurrentUser Account account, @Valid AccountForm accountForm,
                                          Errors errors, Model model, RedirectAttributes attributes) {

        if (errors.hasErrors()) {
            model.addAttribute(account);
            return ACCOUNT;
        }

        if (!accountService.updateNickname(account, accountForm.getNickname())) {
            model.addAttribute(account);
            model.addAttribute(modelMapper.map(account, AccountForm.class));
            model.addAttribute("error", "닉네임 변경은 하루에 한번만 가능합니다.");

            return ACCOUNT;
        }

        attributes.addFlashAttribute("message", "닉네임이 변경되었습니다.");

        return REDIRECT + ACCOUNT;
    }
}
