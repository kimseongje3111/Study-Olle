package com.seongje.studyolle.modules.account.controller;

import com.seongje.studyolle.modules.account.AccountService;
import com.seongje.studyolle.modules.authentication.CurrentUser;
import com.seongje.studyolle.modules.account.form.SignUpForm;
import com.seongje.studyolle.modules.account.repository.AccountRepository;
import com.seongje.studyolle.modules.account.validator.SignUpFormValidator;
import com.seongje.studyolle.domain.account.Account;
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

    static final String MAIN_PATH = "main/";
    static final String ACCOUNT_PATH = "account/";
    static final String SIGN_UP = "sign-up";
    static final String CHECK_EMAIL = "check-email";
    static final String CHECKED_EMAIL = "checked-email";
    static final String EMAIL_LOGIN = "email-login";
    static final String USER_PROFILE = "profile";

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final SignUpFormValidator signUpFormValidator;

    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {

        // 회원 중복 검증 (닉네임, 이메일) //

        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping(SIGN_UP)
    public String signUpForm(Model model) {
        model.addAttribute(new SignUpForm());

        return ACCOUNT_PATH + SIGN_UP;
    }

    @PostMapping(SIGN_UP)
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) {
        if (errors.hasErrors()) {
            return ACCOUNT_PATH + SIGN_UP;
        }

        accountService.processNewAccount(signUpForm);

        return "redirect:/";
    }

    @GetMapping(CHECK_EMAIL)
    public String checkEmail(@CurrentUser Account account, Model model) {
        model.addAttribute("email", account.getEmail());

        return ACCOUNT_PATH + CHECK_EMAIL;
    }

    @GetMapping(CHECK_EMAIL + "-token")
    public String checkEmailToken(String token, String email, Model model) {
        Account findAccount = accountRepository.findByEmail(email);

        if (findAccount == null || !findAccount.isValidToken(token)) {
            model.addAttribute("error", "wrong.emailOrToken");
            return ACCOUNT_PATH + CHECKED_EMAIL;
        }

        accountService.completeSignUpAndCheckEmail(findAccount);

        model.addAttribute("numberOfUser", accountRepository.count());
        model.addAttribute("nickname", findAccount.getNickname());

        return ACCOUNT_PATH + CHECKED_EMAIL;
    }

    @GetMapping(CHECK_EMAIL + "-resend")
    public String resendCheckEmail(@CurrentUser Account account, Model model) {
        if (!account.canResendCheckEmail()) {
            model.addAttribute("error", "인증 이메일은 1시간에 한번만 전송할 수 있습니다.");
            model.addAttribute("email", account.getEmail());

            return ACCOUNT_PATH + CHECK_EMAIL;
        }

        accountService.resendSignUpConfirmEmail(account);

        return "redirect:/";
    }

    @GetMapping(USER_PROFILE + "/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model,
                              @CurrentUser Account account) {
        Account findAccount = accountRepository.findByNickname(nickname);

        if (findAccount == null) {
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");
        }

        model.addAttribute(findAccount);        // TODO : 엔티티 외부 노출
        model.addAttribute("isOwner", findAccount.equals(account));

        return ACCOUNT_PATH + USER_PROFILE;
    }

    @PostMapping(EMAIL_LOGIN)
    public String sendEmailLoginLink(String email, Model model, RedirectAttributes attributes) {
        Account findAccount = accountRepository.findByEmail(email);

        if (findAccount == null) {
            model.addAttribute("error", "유효하지 않은 이메일입니다.");
            return MAIN_PATH + EMAIL_LOGIN;
        }

        if (!findAccount.isEmailVerified()) {
            model.addAttribute("error", "인증되지 않은 이메일입니다. 먼저 이메일 인증을 완료해주세요.");
            return MAIN_PATH + EMAIL_LOGIN;
        }

        if (!findAccount.canResendCheckEmail()) {
            model.addAttribute("error", "이메일 로그인은 1시간에 한 번만 가능합니다.");
            return MAIN_PATH + EMAIL_LOGIN;
        }

        accountService.sendEmailLoginLink(findAccount);
        model.addAttribute("email", email);
        attributes.addFlashAttribute("message", "로그인 링크를 이메일로 전송했습니다.");

        return "redirect:/" + EMAIL_LOGIN;
    }

    @GetMapping(EMAIL_LOGIN + "-confirm")
    public String loginByEmail(String token, String email, Model model) {
        Account findAccount = accountRepository.findByEmail(email);

        if (findAccount == null || !findAccount.isValidToken(token)) {
            model.addAttribute("error", "wrong.emailOrToken");
            return ACCOUNT_PATH + EMAIL_LOGIN + "-confirm";
        }

        accountService.completeEmailLogin(findAccount);

        return ACCOUNT_PATH + EMAIL_LOGIN + "-confirm";
    }
}
