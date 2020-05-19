package com.seongje.studyolle.modules.account;

import com.seongje.studyolle.domain.Account;
import com.seongje.studyolle.modules.account.authentication.CurrentUser;
import com.seongje.studyolle.modules.account.form.ProfileForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountSettingController {

    private final AccountService accountService;

    @InitBinder("profileForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators();
    }

    @GetMapping("/settings/profile")
    public String updateProfileForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new ProfileForm(account));

        return "settings/profile";
    }

    @PostMapping("/settings/profile")
    public String updateProfileFormSubmit(@CurrentUser Account account,
                                          @Valid ProfileForm profileForm,
                                          Errors errors, Model model,
                                          RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);        // TODO : 엔티티 외부 노출
            return "settings/profile";
        }

        accountService.updateProfile(account, profileForm);
        attributes.addFlashAttribute("message", "프로필이 수정되었습니다.");

        return "redirect:/settings/profile";
    }
}
