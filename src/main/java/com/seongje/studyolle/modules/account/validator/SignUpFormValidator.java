package com.seongje.studyolle.modules.account.validator;

import com.seongje.studyolle.modules.account.AccountRepository;
import com.seongje.studyolle.modules.account.form.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator {

    // Custom Validator for Sign up Form //

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(SignUpForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        SignUpForm signUpForm = (SignUpForm) target;

        // 닉네임,이메일 중복 검사 //

        if (accountRepository.existsByNickname(signUpForm.getNickname())) {
            errors.rejectValue("nickname","invalid.nickname",
                    new Object[]{signUpForm.getNickname()}, "이미 사용 중인 닉네임입니다.");
        }

        if (accountRepository.existsByEmail(signUpForm.getEmail())) {
            errors.rejectValue("email","invalid.email",
                    new Object[]{signUpForm.getEmail()}, "이미 등록된 이메일입니다.");
        }
    }
}
