package com.seongje.studyolle.modules.event.repository.custom;

import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.event.domain.Enrollment;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface EnrollmentRepositoryCustom {

    Enrollment searchEnrollmentById(Long enrollmentId);

    Enrollment searchEnrollmentByEventAndAccount(Long eventId, Long accountId);

    List<Enrollment> searchAllByEvent(Long eventId);

    List<Enrollment> searchAllByAccountAndApprovedAndAttended(Long accountId, boolean approved, boolean attended);
}
