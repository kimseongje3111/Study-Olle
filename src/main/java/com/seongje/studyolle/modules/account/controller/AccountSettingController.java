package com.seongje.studyolle.modules.account.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seongje.studyolle.domain.Account;
import com.seongje.studyolle.domain.Tag;
import com.seongje.studyolle.modules.account.AccountService;
import com.seongje.studyolle.modules.account.authentication.CurrentUser;
import com.seongje.studyolle.modules.account.form.AccountForm;
import com.seongje.studyolle.modules.account.form.NotificationsForm;
import com.seongje.studyolle.modules.account.form.PasswordForm;
import com.seongje.studyolle.modules.account.form.ProfileForm;
import com.seongje.studyolle.modules.account.validator.AccountFormValidator;
import com.seongje.studyolle.modules.account.validator.PasswordFormValidator;
import com.seongje.studyolle.modules.tag.TagForm;
import com.seongje.studyolle.modules.tag.TagService;
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
public class AccountSettingController {

    private final AccountService accountService;
    private final TagService tagService;
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

    @GetMapping("/settings/profile")
    public String updateProfileForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);        // TODO : 엔티티 외부 노출
        model.addAttribute(modelMapper.map(account, ProfileForm.class));

        return "settings/profile";
    }

    @PostMapping("/settings/profile")
    public String updateProfileFormSubmit(@CurrentUser Account account,
                                          @Valid ProfileForm profileForm,
                                          Errors errors,
                                          Model model,
                                          RedirectAttributes attributes) {

        if (errors.hasErrors()) {
            model.addAttribute(account);        // TODO : 엔티티 외부 노출
            return "settings/profile";
        }

        accountService.updateProfile(account, profileForm);
        attributes.addFlashAttribute("message", "프로필이 수정되었습니다.");

        return "redirect:/settings/profile";
    }

    @GetMapping("/settings/password")
    public String updatePasswordForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());

        return "settings/password";
    }

    @PostMapping("/settings/password")
    public String updatePasswordFormSubmit(@CurrentUser Account account,
                                           @Valid PasswordForm passwordForm,
                                           Errors errors,
                                           Model model,
                                           RedirectAttributes attributes) {

        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "settings/password";
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "패스워드가 변경되었습니다.");

        return "redirect:/settings/password";
    }

    @GetMapping("/settings/notifications")
    public String updatedNotificationsForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NotificationsForm.class));

        return "settings/notifications";
    }

    @PostMapping("/settings/notifications")
    public String updatedNotificationsFormSubmit(@CurrentUser Account account,
                                                 @Valid NotificationsForm notificationsForm,
                                                 Errors errors,
                                                 Model model,
                                                 RedirectAttributes attributes) {

        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "settings/notifications";
        }

        accountService.updateNotifications(account, notificationsForm);
        attributes.addFlashAttribute("message", "변경 사항이 저장되었습니다.");

        return "redirect:/settings/notifications";
    }

    @GetMapping("settings/tags")
    public String updateTagForm(@CurrentUser Account account, Model model) throws JsonProcessingException {
        List<String> allTags = tagService.getAllTags();
        Set<String> userTags = accountService.getUserTags(account);

        model.addAttribute(account);
        model.addAttribute("tags", userTags);
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return "settings/tags";
    }

    @PostMapping("settings/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentUser Account account, @RequestBody TagForm tagForm) {
        Tag findOrNewTag = tagService.findOrCreateTag(tagForm.getTagTitle());

        accountService.addTag(account, findOrNewTag);

        return ResponseEntity.ok().build();
    }

    @PostMapping("settings/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentUser Account account, @RequestBody TagForm tagForm) {
        Tag findTag = tagService.findByTagTitle(tagForm.getTagTitle());

        if (findTag == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.removeTag(account, findTag);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/settings/account")
    public String updateAccountForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, AccountForm.class));

        return "settings/account";
    }

    @PostMapping("/settings/account")
    public String updateAccountFormSubmit(@CurrentUser Account account,
                                          @Valid AccountForm accountForm,
                                          Errors errors,
                                          Model model,
                                          RedirectAttributes attributes) {

        String view = "settings/account";

        if (errors.hasErrors()) {
            model.addAttribute(account);
            return view;
        }

        if (!accountService.updateNickname(account, accountForm.getNickname())) {
            model.addAttribute(account);
            model.addAttribute(modelMapper.map(account, AccountForm.class));
            model.addAttribute("error", "닉네임 변경은 하루에 한번만 가능합니다.");

            return view;
        }

        attributes.addFlashAttribute("message", "닉네임이 변경되었습니다.");

        return "redirect:/settings/account";
    }
}
