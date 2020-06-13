package com.seongje.studyolle.modules.event.repository.custom;

import com.seongje.studyolle.modules.event.domain.Enrollment;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface EnrollmentRepositoryCustom {

    Enrollment searchEnrollmentById(Long enrollmentId);

    Enrollment searchEnrollmentByEventAndAccount(Long eventId, Long accountId);
}
