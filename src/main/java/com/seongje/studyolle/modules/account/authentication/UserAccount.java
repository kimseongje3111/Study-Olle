package com.seongje.studyolle.modules.account.authentication;

import com.seongje.studyolle.modules.account.domain.Account;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

@Getter
public class UserAccount extends User {

    private Account account;

    public UserAccount(Account account) {
        super(account.getNickname(), account.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        this.account = account;
    }
}