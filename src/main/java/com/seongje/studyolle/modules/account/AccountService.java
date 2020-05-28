package com.seongje.studyolle.modules.account;

import com.seongje.studyolle.domain.*;
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
import com.seongje.studyolle.modules.tag.TagRepository;
import com.seongje.studyolle.modules.zone.ZoneRepository;
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

import javax.mail.MessagingException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final TagItemRepository tagItemRepository;
    private final TagRepository tagRepository;
    private final ZoneItemRepository zoneItemRepository;
    private final ZoneRepository zoneRepository;

    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    private final ModelMapper modelMapper;

    @Transactional
    public void processNewAccount(SignUpForm signUpForm) {
        Account newAccount = helloNewAccount(signUpForm);

        sendSignUpConfirmEmail(newAccount);
        login(newAccount);
    }

    @Transactional
    public void completeSignUpAndCheckEmail(Account newAccount) {
        newAccount.completeSignUpAndCheckEmail();
        login(newAccount);
    }

    public void resendSignUpConfirmEmail(Account account) {
        sendSignUpConfirmEmail(account);
    }

    @Transactional
    public void sendEmailLoginLink(Account account) {
        account.generateCheckEmailToken();
        sendLoginEmail(account);
    }

    public void completeEmailLogin(Account account) {
        login(account);
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

    @Transactional
    public void updateProfile(Account account, ProfileForm profileForm) {
        modelMapper.map(profileForm, account);
        accountRepository.save(account);
    }

    @Transactional
    public void updatePassword(Account account, String newPassword) {
        account.changePassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    @Transactional
    public void updateNotifications(Account account, NotificationsForm notificationsForm) {
        modelMapper.map(notificationsForm, account);
        accountRepository.save(account);
    }

    @Transactional
    public boolean updateNickname(Account account, String newNickname) {
        Account findAccount = accountRepository.findByNickname(account.getNickname());

        if (!findAccount.canChangeNickName()) return false;

        account.changeNickname(newNickname);
        accountRepository.save(account);
        login(account);     // principal update

        return true;
    }

    @Transactional
    public void addTag(Account account, Tag tag) {
        Account findAccount = accountRepository.findByEmail(account.getEmail());
        Tag findTag = tagRepository.findByTitle(tag.getTitle());

        if (findAccount != null) {
            TagItem newTagItem = tagItemRepository.save(TagItem.createTagItem(findAccount, findTag));
            findAccount.addTagItem(newTagItem);
        }
    }

    @Transactional
    public void removeTag(Account account, Tag tag) {
        Account findAccount = accountRepository.findByEmail(account.getEmail());
        Tag findTag = tagRepository.findByTitle(tag.getTitle());

        if (findAccount != null) {
            findAccount.removeTagItem(findTag);
        }
    }

    public Set<String> getUserTags(Account account) throws IllegalStateException {
        Optional<Account> findAccount = accountRepository.findById(account.getId());
        Set<TagItem> tags = findAccount.orElseThrow(IllegalStateException::new).getTags();

        return tags.stream().map(tagItem -> tagItem.getTag().getTitle()).collect(Collectors.toSet());
    }

    @Transactional
    public void addZone(Account account, Zone zone) {
        Account findAccount = accountRepository.findByEmail(account.getEmail());
        Zone findZone = zoneRepository.findByCityAndLocalNameOfCity(zone.getCity(), zone.getLocalNameOfCity());

        if (findAccount != null) {
            ZoneItem newZoneItem = zoneItemRepository.save(ZoneItem.createZoneItem(findAccount, findZone));
            findAccount.addZoneItem(newZoneItem);
        }
    }

    @Transactional
    public void removeZone(Account account, Zone zone) {
        Account findAccount = accountRepository.findByEmail(account.getEmail());
        Zone findZone = zoneRepository.findByCityAndLocalNameOfCity(zone.getCity(), zone.getLocalNameOfCity());

        if (findAccount != null) {
            findAccount.removeZoneItem(findZone);
        }
    }

    public Set<String> getUserZones(Account account) throws IllegalStateException {
        Optional<Account> findAccount = accountRepository.findById(account.getId());
        Set<ZoneItem> zones = findAccount.orElseThrow(IllegalStateException::new).getZones();

        return zones.stream().map(zoneItem -> zoneItem.getZone().toString()).collect(Collectors.toSet());
    }

    private Account helloNewAccount(SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account newAccount = modelMapper.map(signUpForm, Account.class);

        newAccount.generateCheckEmailToken();

        return accountRepository.save(newAccount);
    }

    private void sendSignUpConfirmEmail(Account newAccount) {
        EmailMessage emailMessage = EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("[스터디올래] 회원 가입 완료를 위한 계정 인증 메일입니다.")
                .text(getSignUpConfirmEmail(newAccount))
                .build();

        mailService.send(emailMessage);
    }

    private void sendLoginEmail(Account account) {
        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("[스터디올래] 이메일 로그인 링크입니다.")
                .text(getLoginEmail(account))
                .build();

        mailService.send(emailMessage);
    }

    private String getSignUpConfirmEmail(Account account) {

        // thymeleaf context //

        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("message", "스터디올래 서비스를 사용하기 위해 아래 링크를 클릭하여 인증을 완료해주세요.");
        context.setVariable("link", "/check-email-token?token=" + account.getEmailCheckToken() + "&email=" + account.getEmail());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("host", appProperties.getHost());

        return templateEngine.process("mail/simple-email-template", context);
    }

    private String getLoginEmail(Account account) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("message", "로그인 하려면 아래 링크를 클릭하세요.");
        context.setVariable("link", "/email-login-confirm?token=" + account.getEmailCheckToken() + "&email=" + account.getEmail());
        context.setVariable("linkName", "스터디올래 로그인하기");
        context.setVariable("host", appProperties.getHost());

        return templateEngine.process("mail/simple-email-template", context);
    }

    private void login(Account newAccount) {

        // SecurityContext 에 Authentication(Token) 이 존재하는가 //

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(newAccount),   // 현재 인증된 Principal
                newAccount.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContextHolder.getContext().setAuthentication(token);
    }
}
