package com.seongje.studyolle.modules.event.repository;

import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.event.domain.Enrollment;
import com.seongje.studyolle.modules.event.domain.Event;
import com.seongje.studyolle.modules.event.repository.custom.EnrollmentRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long>, EnrollmentRepositoryCustom {

    boolean existsByEventAndAccount(Event event, Account account);
}
