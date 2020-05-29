package com.seongje.studyolle.modules.study;

import com.seongje.studyolle.domain.account.Account;
import com.seongje.studyolle.domain.study.Study;
import com.seongje.studyolle.domain.study.StudyMember;
import com.seongje.studyolle.modules.account.repository.AccountRepository;
import com.seongje.studyolle.modules.study.repository.StudyMemberRepository;
import com.seongje.studyolle.modules.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.seongje.studyolle.domain.study.ManagementLevel.*;
import static com.seongje.studyolle.domain.study.StudyMember.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyService {

    private final AccountRepository accountRepository;
    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;

    @Transactional
    public Study createNewStudy(Study newStudyInfo, Account account) {
        Account findAccount = accountRepository.findByEmail(account.getEmail());
        Study newStudy = studyRepository.save(newStudyInfo);

        if (findAccount != null) {
            StudyMember newStudyMember = studyMemberRepository.save(createStudyMember(newStudy, findAccount, MANAGER));
            newStudy.addStudyMember(newStudyMember);

            return newStudy;
        }

        return null;
    }

    public Study findStudy(String path) {
        Study findStudy = studyRepository.findByPath(path);

        if (findStudy == null) throw new IllegalStateException("요청한 '" + path + "'에 해당하는 스터디가 없습니다.");

        return findStudy;
    }
}
