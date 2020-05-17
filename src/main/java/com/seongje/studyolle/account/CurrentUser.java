package com.seongje.studyolle.account;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : account")
public @interface CurrentUser {

    // Custom annotation //

    // 스프링 시큐리티에 의해 인증되지 않은 사용자 : null //
    // 파라미터로 적용, 런타임까지 유지 //
    // @AuthenticationPrincipal : SpEL을 사용해 Principal 내부 정보 접근 //
}
