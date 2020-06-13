package com.seongje.studyolle.modules.account;

import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountFactory {

    @Autowired
    AccountRepository accountRepository;

    public Account createAccount(String nickname) {
        Account account = new Account();

        account.setNickname(nickname);
        account.setEmail(nickname + "@email.com");
        account.setPassword("password");
        accountRepository.save(account);

        return account;
    }
}
