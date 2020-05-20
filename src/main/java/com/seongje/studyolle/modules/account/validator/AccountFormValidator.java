package com.seongje.studyolle.modules.account.validator;

import com.seongje.studyolle.modules.account.AccountRepository;
import com.seongje.studyolle.modules.account.form.AccountForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class AccountFormValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(AccountForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        AccountForm accountForm = (AccountForm) target;

        if (accountRepository.existsByNickname(accountForm.getNickname())) {
            errors.rejectValue("nickname","invalid.nickname",
                    new Object[]{accountForm.getNickname()}, "이미 사용 중인 닉네임입니다.");
        }
    }
}
