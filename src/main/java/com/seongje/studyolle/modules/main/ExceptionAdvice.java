package com.seongje.studyolle.modules.main;

import com.seongje.studyolle.modules.account.authentication.CurrentUser;
import com.seongje.studyolle.modules.account.domain.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler
    public String handleRuntimeException(@CurrentUser Account account, HttpServletRequest request, RuntimeException e) {
        if (account != null) {
            log.info("'{}' requested '{}'", account.getNickname(), request.getRequestURI());

        } else {
            log.info("requested '{}'", request.getRequestURI());
        }

        log.error("bad request", e);

        return "error";
    }
}
