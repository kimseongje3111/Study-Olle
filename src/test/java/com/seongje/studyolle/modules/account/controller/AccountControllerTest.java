package com.seongje.studyolle.modules.account.controller;

import com.seongje.studyolle.infra.AbstractContainerBaseTest;
import com.seongje.studyolle.infra.MockMvcTest;
import com.seongje.studyolle.infra.mail.EmailMessage;
import com.seongje.studyolle.infra.mail.MailService;
import com.seongje.studyolle.modules.account.AccountFactory;
import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.account.repository.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.seongje.studyolle.modules.account.controller.AccountController.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class AccountControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired AccountFactory accountFactory;

    @MockBean
    MailService mailService;

    @Test
    @DisplayName("회원 가입 - 폼")
    void signUpForm() throws Exception {
        mockMvc.perform(get("/" + SIGN_UP))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(view().name(SIGN_UP));
    }

    @Test
    @DisplayName("회원 가입 - 정상 처리")
    void signUpSubmit() throws Exception {
        mockMvc.perform(post("/" + SIGN_UP)
                .param("nickname", "seongje")
                .param("email", "abcd@email.com")
                .param("password","12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated());

        // when
        Account findAccount = accountRepository.findByEmail("abcd@email.com");

        // then
        assertThat(accountRepository.existsByEmail("abcd@email.com")).isTrue();
        assertThat(findAccount.getPassword()).isNotEqualTo("12345678");
        assertThat(findAccount.getEmailCheckToken()).isNotNull();

        then(mailService).should().send(any(EmailMessage.class));
    }

    @Test
    @DisplayName("인증 메일 확인 실패 - 입력 값 오류")
    void checkEmailToken_with_wrong_input() throws Exception {
        mockMvc.perform(get("/" + CHECK_EMAIL_TOKEN)
                .param("token", "abcdefg")
                .param("email","a@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name(CHECKED_EMAIL))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("인증 메일 확인 - 정상 처리")
    void checkEmailToken() throws Exception {
        // Given
        Account account = accountFactory.createAccount("seongje");

        // When
        account.generateCheckEmailToken();

        // Then
        mockMvc.perform(get("/" + CHECK_EMAIL_TOKEN)
                .param("token", account.getEmailCheckToken())
                .param("email", account.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(view().name(CHECKED_EMAIL))
                .andExpect(authenticated());
    }

}