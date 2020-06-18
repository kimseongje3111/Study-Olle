package com.seongje.studyolle.modules.account.controller;

import com.seongje.studyolle.modules.account.service.AccountService;
import com.seongje.studyolle.modules.account.authentication.CurrentUser;
import com.seongje.studyolle.modules.account.form.SignUpForm;
import com.seongje.studyolle.modules.account.validator.SignUpFormValidator;
import com.seongje.studyolle.modules.account.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class AccountController {

    static final String REDIRECT = "redirect:/";
    static final String LOGIN = "account/login";
    static final String SIGN_UP = "account/sign-up";
    static final String CHECK_EMAIL = "account/check-email";
    static final String CHECK_EMAIL_TOKEN = "account/check-email-token";
    static final String CHECK_EMAIL_RESEND = "account/check-email-resend";
    static final String CHECKED_EMAIL = "account/checked-email";
    static final String EMAIL_LOGIN = "account/email-login";
    static final String EMAIL_LOGIN_CONFIRM = "account/email-login-confirm";
    static final String USER_PROFILE = "account/profile/{nickname}";

    private final AccountService accountService;
    private final SignUpFormValidator signUpFormValidator;

    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping(SIGN_UP)
    public String signUpForm(Model model) {
        model.addAttribute(new SignUpForm());

        return SIGN_UP;
    }

    @PostMapping(SIGN_UP)
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) {
        if (errors.hasErrors()) {
            return SIGN_UP;
        }

        accountService.processForNewAccount(signUpForm);

        return REDIRECT;
    }

    @GetMapping(CHECK_EMAIL)
    public String checkEmail(@CurrentUser Account account, Model model) {
        model.addAttribute(account);

        return CHECK_EMAIL;
    }

    @GetMapping(CHECK_EMAIL_TOKEN)
    public String checkEmailToken(String token, String email, Model model) {
        Account findAccount = accountService.findByEmailAsNullable(email);

        if (findAccount == null || !findAccount.isValidToken(token)) {
            model.addAttribute("error", "wrong.emailOrToken");
            return CHECKED_EMAIL;
        }

        accountService.completeSignUpAndCheckEmail(findAccount);
        model.addAttribute(findAccount);
        model.addAttribute("numberOfUser", accountService.usersTotalCount());

        return CHECKED_EMAIL;
    }

    @GetMapping(CHECK_EMAIL_RESEND)
    public String resendCheckEmail(@CurrentUser Account account, Model model) {
        if (!account.canResendCheckEmail()) {
            model.addAttribute("error", "인증 이메일은 1시간에 한번만 전송할 수 있습니다.");
            model.addAttribute(account);

            return CHECK_EMAIL;
        }

        accountService.sendSignUpConfirmEmail(account);

        return REDIRECT;
    }

    @GetMapping(LOGIN)
    public String login() {
        return LOGIN;
    }

    @GetMapping(EMAIL_LOGIN)
    public String emailLogin() {
        return EMAIL_LOGIN;
    }

    @PostMapping(EMAIL_LOGIN)
    public String sendEmailLoginLink(String email, Model model, RedirectAttributes attributes) {
        Account findAccount = accountService.findByEmailAsNullable(email);

        if (findAccount == null) {
            model.addAttribute("error", "유효하지 않은 이메일입니다.");
            return EMAIL_LOGIN;
        }

        if (!findAccount.isEmailVerified()) {
            model.addAttribute("error", "인증되지 않은 이메일입니다. 먼저 이메일 인증을 완료해주세요.");
            return EMAIL_LOGIN;
        }

        if (!findAccount.canResendCheckEmail()) {
            model.addAttribute("error", "이메일 로그인은 1시간에 한 번만 가능합니다.");
            return EMAIL_LOGIN;
        }

        accountService.sendEmailLoginLink(findAccount);
        model.addAttribute(findAccount);
        attributes.addFlashAttribute("message", "로그인 링크를 이메일로 전송했습니다.");

        return REDIRECT + EMAIL_LOGIN;
    }

    @GetMapping(EMAIL_LOGIN_CONFIRM)
    public String loginByEmail(String token, String email, Model model) {
        Account findAccount = accountService.findByEmailAsNullable(email);

        if (findAccount == null || !findAccount.isValidToken(token)) {
            model.addAttribute("error", "wrong.emailOrToken");
            return EMAIL_LOGIN_CONFIRM;
        }

        accountService.login(findAccount);
        model.addAttribute(findAccount);


        return EMAIL_LOGIN_CONFIRM;
    }

    @GetMapping(USER_PROFILE)
    public String viewProfile(@CurrentUser Account account, @PathVariable String nickname, Model model) {
        Account findAccount = accountService.findByNickname(nickname);

        model.addAttribute(findAccount);
        model.addAttribute("isOwner", findAccount.equals(account));

        return "account/profile";
    }
}
