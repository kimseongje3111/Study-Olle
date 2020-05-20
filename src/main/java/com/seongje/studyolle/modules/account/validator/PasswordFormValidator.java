package com.seongje.studyolle.modules.account.validator;

import com.seongje.studyolle.modules.account.form.PasswordForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class PasswordFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(PasswordForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PasswordForm passwordForm = (PasswordForm) target;

        if (!passwordForm.getNewPassword().equals(passwordForm.getNewPasswordConfirm())) {
            errors.rejectValue("newPasswordConfirm", "wrong.value", "입력한 패스워드가 일치하지 않습니다.");
        }
    }
}
