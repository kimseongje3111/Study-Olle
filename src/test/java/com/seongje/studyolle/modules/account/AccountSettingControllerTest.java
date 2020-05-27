package com.seongje.studyolle.modules.account;

import com.seongje.studyolle.domain.Account;
import com.seongje.studyolle.modules.account.repository.AccountRepository;
import com.seongje.studyolle.modules.authentication.WithAccount;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountSettingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("프로필 수정 - 정상 입력")
    @WithAccount(value = "seongje")
    void updateProfileForm() throws Exception {
        // given
        String content = "안녕하세요.";

        mockMvc.perform(post("/settings/profile")
                .param("aboutMe", content)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/profile"))
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
        mockMvc.perform(get("/settings/password"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(view().name("settings/password"));
    }

    @Test
    @DisplayName("패스워드 수정 - 정상 입력")
    @WithAccount("seongje")
    void updatePassWordForm() throws Exception {
        // given
        String newPassword = "123456789";

        mockMvc.perform(post("/settings/password")
                .param("newPassword", newPassword)
                .param("newPasswordConfirm", newPassword)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/password"))
                .andExpect(flash().attributeExists("message"));

        // when
        Account account = accountRepository.findByNickname("seongje");

        // then
        assertThat(passwordEncoder.matches(newPassword, account.getPassword())).isTrue();
    }

    @Test
    @DisplayName("패스워드 수정 - 일치하지 않은 패스워드")
    @WithAccount("seongje")
    void updatePassWordForm_password_confirm_not_equal() throws Exception {
        mockMvc.perform(post("/settings/password")
                .param("newPassword", "123456789")
                .param("newPasswordConfirm", "987654321")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(view().name("settings/password"));
    }

    @Test
    @DisplayName("닉네임 수정 - 닉네임 중복")
    @WithAccount("seongje")
    void updateAccountForm_nickname_duplication() throws Exception {
        mockMvc.perform(post("/settings/account")
                .param("nickname", "seongje")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("accountForm"))
                .andExpect(view().name("settings/account"));
    }

    @Test
    @DisplayName("닉네임 수정 - 변경 불가(하루에 한번 이상)")
    @WithAccount("seongje")
    void updateAccountForm_nickname_change_twiceADay() throws Exception {
        mockMvc.perform(post("/settings/account")
                .param("nickname", "newName")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("accountForm"))
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("settings/account"));
    }
}