package com.seongje.studyolle.modules.account.form;

import com.seongje.studyolle.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class NotificationsForm {

    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb;
}
