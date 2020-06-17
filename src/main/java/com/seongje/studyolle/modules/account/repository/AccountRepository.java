package com.seongje.studyolle.modules.account.repository;

import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.account.repository.custom.AccountRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<Account, Long>, AccountRepositoryCustom {

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);

    Account findByEmail(String email);

    Account findByNickname(String nickname);
}
