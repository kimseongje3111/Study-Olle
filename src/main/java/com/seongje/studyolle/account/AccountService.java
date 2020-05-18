package com.seongje.studyolle.account;

import com.seongje.studyolle.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = helloNewAccount(signUpForm);

        newAccount.generateCheckEmailToken();
        sendSignUpConfirmEmail(newAccount);

        return newAccount;
    }

    public void login(Account newAccount) {

        // SecurityContext 에 Authentication(Token) 이 존재하는가 //

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(newAccount),   // 현재 인증된 Principal
                newAccount.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContextHolder.getContext().setAuthentication(token);
    }

    public void resendSignUpConfirmEmail(Account newAccount) {
        sendSignUpConfirmEmail(newAccount);
    }

    @Override
    public UserDetails loadUserByUsername(String emailOrUsername) throws UsernameNotFoundException {

        // Login administration //

        Account account = accountRepository.findByEmail(emailOrUsername);

        if (account == null) {
            account = accountRepository.findByNickname(emailOrUsername);
        }

        if (account == null) {
            throw new UsernameNotFoundException(emailOrUsername);
        }

        return new UserAccount(account);
    }

    private void sendSignUpConfirmEmail(Account newAccount) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(newAccount.getEmail());
        simpleMailMessage.setSubject("[스터디올래] 회원 가입 완료를 위한 계정 인증 메일입니다.");
        simpleMailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken()
                + "&email=" + newAccount.getEmail());

        javaMailSender.send(simpleMailMessage);
    }

    private Account helloNewAccount(SignUpForm signUpForm) {
        Account newAccount = Account.builder()
                .nickname(signUpForm.getNickname())
                .email(signUpForm.getEmail())
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdatedByWeb(true)
                .build();

        accountRepository.save(newAccount);

        return newAccount;
    }
}
