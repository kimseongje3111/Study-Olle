package com.seongje.studyolle.modules.account.controller;

import com.seongje.studyolle.modules.account.AccountService;
import com.seongje.studyolle.modules.account.authentication.CurrentUser;
import com.seongje.studyolle.modules.account.form.SignUpForm;
import com.seongje.studyolle.modules.account.repository.AccountRepository;
import com.seongje.studyolle.modules.account.validator.SignUpFormValidator;
import com.seongje.studyolle.domain.Account;
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
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;

    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {

        // 회원 중복 검증 (닉네임, 이메일) //

        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute(new SignUpForm());

        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) {
        if (errors.hasErrors()) {
            return "account/sign-up";
        }

        accountService.processNewAccount(signUpForm);

        return "redirect:/";
    }

    @GetMapping("/check-email")
    public String checkEmail(@CurrentUser Account account, Model model) {
        model.addAttribute("email", account.getEmail());

        return "account/check-email";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model) {
        Account findAccount = accountRepository.findByEmail(email);
        String view = "account/checked-email";

        if (findAccount == null || !findAccount.isValidToken(token)) {
            model.addAttribute("error", "wrong.emailOrToken");
            return view;
        }

        accountService.completeSignUpAndCheckEmail(findAccount);

        model.addAttribute("numberOfUser", accountRepository.count());
        model.addAttribute("nickname", findAccount.getNickname());

        return view;
    }

    @GetMapping("/resend-confirm-email")
    public String resendConfirmEmail(@CurrentUser Account account, Model model) {
        if (!account.canResendConfirmEmail()) {
            model.addAttribute("error", "인증 이메일은 1시간에 한번만 전송할 수 있습니다.");
            model.addAttribute("email", account.getEmail());

            return "account/check-email";
        }

        accountService.resendSignUpConfirmEmail(account);

        return "redirect:/";
    }

    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model,
                              @CurrentUser Account account) {
        Account findAccount = accountRepository.findByNickname(nickname);

        if (findAccount == null) {
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");
        }

        model.addAttribute(findAccount);        // TODO : 엔티티 외부 노출
        model.addAttribute("isOwner", findAccount.equals(account));

        return "account/profile";
    }

    @PostMapping("/email-login")
    public String sendEmailLoginLink(String email, Model model, RedirectAttributes attributes) {
        Account findAccount = accountRepository.findByEmail(email);
        String view = "main/email-login";

        if (findAccount == null) {
            model.addAttribute("error", "유효하지 않은 이메일입니다.");
            return view;
        }

        if (!findAccount.isEmailVerified()) {
            model.addAttribute("error", "인증되지 않은 이메일입니다. 먼저 이메일 인증을 완료해주세요.");
            return view;
        }

        if (!findAccount.canResendConfirmEmail()) {
            model.addAttribute("error", "이메일 로그인은 1시간에 한 번만 가능합니다.");
            return view;
        }

        accountService.sendEmailLoginLink(findAccount);
        model.addAttribute("email", email);
        attributes.addFlashAttribute("message", "로그인 링크를 이메일로 전송했습니다.");

        return "redirect:/email-login";
    }

    @GetMapping("/login-by-email")
    public String loginByEmail(String token, String email, Model model) {
        Account findAccount = accountRepository.findByEmail(email);
        String view = "account/login-by-email";

        if (findAccount == null || !findAccount.isValidToken(token)) {
            model.addAttribute("error", "wrong.emailOrToken");
            return view;
        }

        accountService.completeEmailLogin(findAccount);

        return view;
    }
}
