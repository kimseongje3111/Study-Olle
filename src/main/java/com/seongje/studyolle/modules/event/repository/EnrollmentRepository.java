package com.seongje.studyolle.modules.event.repository;

import com.seongje.studyolle.domain.event.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
}
