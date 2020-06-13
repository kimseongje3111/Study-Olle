package com.seongje.studyolle.modules.account.controller;

import com.seongje.studyolle.infra.AbstractContainerBaseTest;
import com.seongje.studyolle.infra.MockMvcTest;
import com.seongje.studyolle.modules.account.form.SignUpForm;
import com.seongje.studyolle.modules.account.repository.AccountRepository;
import com.seongje.studyolle.modules.account.service.AccountService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static com.seongje.studyolle.modules.account.controller.AccountController.LOGIN;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcTest
public class LoginAndLogoutTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;

    @BeforeEach
    void beforeEach() {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("seongje");
        signUpForm.setEmail("seongje@email.com");
        signUpForm.setPassword("password");

        accountService.processNewAccount(signUpForm);
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("이메일 로그인")
    void login_with_email() throws Exception {
        mockMvc.perform(post("/" + LOGIN)
                .param("username", "seongje@email.com")
                .param("password", "password")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("seongje"));
    }

    @Test
    @DisplayName("닉네임 로그인")
    void login_with_nickname() throws Exception {
        mockMvc.perform(post("/" + LOGIN)
                .param("username", "seongje")
                .param("password", "password")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("seongje"));
    }

    @Test
    @DisplayName("로그인 실패")
    void login_fail() throws Exception {
        mockMvc.perform(post("/" + LOGIN)
                .param("nickname", "wrongInput")
                .param("password", "wrongInput")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/" + LOGIN + "?error"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("로그아웃")
    @WithMockUser
    void logout() throws Exception {
        mockMvc.perform(post("/logout")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
    }
}
