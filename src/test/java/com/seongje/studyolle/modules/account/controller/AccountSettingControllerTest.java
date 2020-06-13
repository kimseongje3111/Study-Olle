package com.seongje.studyolle.modules.account.controller;

import com.seongje.studyolle.infra.AbstractContainerBaseTest;
import com.seongje.studyolle.infra.MockMvcTest;
import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.account.repository.AccountRepository;
import com.seongje.studyolle.modules.account.authentication.WithAccount;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.seongje.studyolle.modules.account.controller.AccountSettingController.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
@RequestMapping("/")
class AccountSettingControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("프로필 수정 - 정상 처리")
    @WithAccount(value = "seongje")
    void updateProfileForm() throws Exception {
        // given
        String content = "안녕하세요.";

        mockMvc.perform(post("/" + PROFILE)
                .param("aboutMe", content)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/" + PROFILE))
                .andExpect(flash().attributeExists("message"));

        // when
        Account account = accountRepository.findByNickname("seongje");

        // then
        assertThat(account.getAboutMe()).isEqualTo(content);
    }
    
    @Test
    @DisplayName("패스워드 수정 - 폼")
    @WithAccount("seongje")
    void passWordForm() throws Exception {
        mockMvc.perform(get("/" + PASSWORD))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(view().name(PASSWORD));
    }

    @Test
    @DisplayName("패스워드 수정 - 정상 처리")
    @WithAccount("seongje")
    void updatePassWordForm() throws Exception {
        // given
        String newPassword = "123456789";

        mockMvc.perform(post("/" + PASSWORD)
                .param("newPassword", newPassword)
                .param("newPasswordConfirm", newPassword)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/" + PASSWORD))
                .andExpect(flash().attributeExists("message"));

        // when
        Account account = accountRepository.findByNickname("seongje");

        // then
        assertThat(passwordEncoder.matches(newPassword, account.getPassword())).isTrue();
    }

    @Test
    @DisplayName("패스워드 수정 실패 - 일치하지 않은 패스워드")
    @WithAccount("seongje")
    void updatePassWordForm_password_confirm_not_equal() throws Exception {
        mockMvc.perform(post("/" + PASSWORD)
                .param("newPassword", "123456789")
                .param("newPasswordConfirm", "987654321")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(view().name(PASSWORD));
    }

    @Test
    @DisplayName("닉네임 수정 실패 - 닉네임 중복")
    @WithAccount("seongje")
    void updateAccountForm_nickname_duplication() throws Exception {
        mockMvc.perform(post("/" + ACCOUNT)
                .param("nickname", "seongje")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("accountForm"))
                .andExpect(view().name(ACCOUNT));
    }

    @Test
    @DisplayName("닉네임 수정 실패 - 하루에 한번 이상")
    @WithAccount("seongje")
    void updateAccountForm_nickname_change_twiceADay() throws Exception {
        mockMvc.perform(post("/" + ACCOUNT)
                .param("nickname", "newName1")
                .with(csrf()));

        mockMvc.perform(post("/" + ACCOUNT)
                .param("nickname", "newName2")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("accountForm"))
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name(ACCOUNT));
    }

}