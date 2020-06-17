package com.seongje.studyolle.modules.study.app_event.custom;

import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.study.domain.Study;
import lombok.Getter;

@Getter
public class StudyMemberEvent extends StudyAppEvent {

    private Account account;
    private StudyMemberEventType eventType;

    public StudyMemberEvent(Study study, Account account, StudyMemberEventType eventType) {
        super(study, "스터디 가입/탈퇴");
        this.account = account;
        this.eventType = eventType;
    }
}
