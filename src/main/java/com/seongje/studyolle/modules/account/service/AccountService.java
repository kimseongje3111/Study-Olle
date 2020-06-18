package com.seongje.studyolle.modules.account.service;

import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.account.domain.TagItem;
import com.seongje.studyolle.modules.account.domain.ZoneItem;
import com.seongje.studyolle.infra.config.AppProperties;
import com.seongje.studyolle.infra.mail.EmailMessage;
import com.seongje.studyolle.infra.mail.MailService;
import com.seongje.studyolle.modules.account.authentication.UserAccount;
import com.seongje.studyolle.modules.account.form.NotificationsForm;
import com.seongje.studyolle.modules.account.form.ProfileForm;
import com.seongje.studyolle.modules.account.form.SignUpForm;
import com.seongje.studyolle.modules.account.repository.AccountRepository;
import com.seongje.studyolle.modules.account.repository.TagItemRepository;
import com.seongje.studyolle.modules.account.repository.ZoneItemRepository;
import com.seongje.studyolle.modules.tag.domain.Tag;
import com.seongje.studyolle.modules.zone.domain.Zone;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.seongje.studyolle.modules.account.domain.TagItem.*;
import static com.seongje.studyolle.modules.account.domain.ZoneItem.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final TagItemRepository tagItemRepository;
    private final ZoneItemRepository zoneItemRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final ModelMapper modelMapper;

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

    public Account findByEmail(String email) {
        Account findAccount = accountRepository.findByEmail(email);

        if (findAccount == null) {
            throw new IllegalArgumentException(email + "에 해당하는 사용자가 없습니다.");
        }

        return findAccount;
    }

    public Account findByNickname(String nickname) {
        Account findAccount = accountRepository.findByNickname(nickname);

        if (findAccount == null) {
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");
        }

        return findAccount;
    }

    public Account findByEmailAsNullable(String email) {
        return accountRepository.findByEmail(email);
    }

    public long usersTotalCount() {
        return accountRepository.count();
    }

    @Transactional
    public void processForNewAccount(SignUpForm signUpForm) {
        Account newAccount = createdNewAccount(signUpForm);

        sendSignUpConfirmEmail(newAccount);
        login(newAccount);
    }

    public void sendSignUpConfirmEmail(Account account) {
        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("[스터디올래] 회원 가입 완료를 위한 계정 인증 메일입니다.")
                .text(getSignUpConfirmEmailContent(account))
                .build();

        mailService.send(emailMessage);
    }

    public void login(Account account) {

        // SecurityContext 에 Authentication(Token) 이 존재하는가 //

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),   // 현재 인증된 Principal
                account.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Transactional
    public void completeSignUpAndCheckEmail(Account account) {
        account.completeSignUpAndCheckEmail();
        login(account);
    }

    @Transactional
    public void sendEmailLoginLink(Account account) {
        account.generateCheckEmailToken();
        sendLoginEmail(account);
    }

    @Transactional
    public void updateProfile(Account account, ProfileForm profileForm) {
        modelMapper.map(profileForm, account);
        login(account);
    }

    @Transactional
    public void updatePassword(Account account, String newPassword) {
        account.changePassword(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public void updateNotifications(Account account, NotificationsForm notificationsForm) {
        modelMapper.map(notificationsForm, account);
        login(account);
    }

    public Set<String> getUserTags(Account account) throws IllegalStateException {
        Optional<Account> findAccount = accountRepository.findById(account.getId());
        Set<TagItem> tags = findAccount.orElseThrow(IllegalStateException::new).getTags();

        return tags.stream()
                .map(tagItem -> tagItem.getTag().getTitle())
                .collect(Collectors.toSet());
    }

    @Transactional
    public void addTag(Account account, Tag tag) {
        account.addTagItem(
                tagItemRepository.save(createTagItem(account, tag))
        );
    }

    @Transactional
    public void removeTag(Account account, Tag tag) {
        account.removeTagItem(tag);
    }

    public Set<String> getUserZones(Account account) throws IllegalStateException {
        Optional<Account> findAccount = accountRepository.findById(account.getId());
        Set<ZoneItem> zones = findAccount.orElseThrow(IllegalStateException::new).getZones();

        return zones.stream()
                .map(zoneItem -> zoneItem.getZone().toString())
                .collect(Collectors.toSet());
    }

    @Transactional
    public void addZone(Account account, Zone zone) {
        account.addZoneItem(
                zoneItemRepository.save(createZoneItem(account, zone))
        );
    }

    @Transactional
    public void removeZone(Account account, Zone zone) {
        account.removeZoneItem(zone);
    }

    @Transactional
    public boolean updateNickname(Account account, String newNickname) {
        if (!account.canChangeNickName()) {
            return false;
        }

        account.changeNickname(newNickname);
        login(account);     // principal update

        return true;
    }

    private Account createdNewAccount(SignUpForm signUpForm) {
        Account newAccount = modelMapper.map(signUpForm, Account.class);

        newAccount.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        newAccount.generateCheckEmailToken();

        return accountRepository.save(newAccount);
    }

    private String getSignUpConfirmEmailContent(Account account) {

        // thymeleaf context //

        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("message", "스터디올래 서비스를 사용하기 위해 아래 링크를 클릭하여 인증을 완료해주세요.");
        context.setVariable("link", "/account/check-email-token?token=" + account.getEmailCheckToken() + "&email=" + account.getEmail());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("host", appProperties.getHost());

        return templateEngine.process("mail/simple-email-template", context);
    }

    private void sendLoginEmail(Account account) {
        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("[스터디올래] 이메일 로그인 링크입니다.")
                .text(getLoginEmailContent(account))
                .build();

        mailService.send(emailMessage);
    }

    private String getLoginEmailContent(Account account) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("message", "로그인 하려면 아래 링크를 클릭하세요.");
        context.setVariable("link", "/account/email-login-confirm?token=" + account.getEmailCheckToken() + "&email=" + account.getEmail());
        context.setVariable("linkName", "스터디올래 로그인하기");
        context.setVariable("host", appProperties.getHost());

        return templateEngine.process("mail/simple-email-template", context);
    }
}
