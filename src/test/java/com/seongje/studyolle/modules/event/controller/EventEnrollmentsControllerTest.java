package com.seongje.studyolle.modules.event.controller;

import com.seongje.studyolle.infra.AbstractContainerBaseTest;
import com.seongje.studyolle.infra.MockMvcTest;
import com.seongje.studyolle.modules.account.authentication.WithAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@MockMvcTest
class EventEnrollmentsControllerTest extends AbstractContainerBaseTest {

    @Test
    @DisplayName("관리자 확인 모임 - 모집 인원 내 모임 신청 대기자에 대해 관리자 확인 (확정)")
    @WithAccount("seongje")
    public void enrollment_accept_from_MANAGER_APPROVAL_event_when_there_are_remains() throws Exception {
        // Given

        // When

        // Then
    }

    @Test
    @DisplayName("관리자 확인 모임 - 모집 인원 초과시 모임 신청 대기자에 대해 관리자 확인 (변경 불가)")
    @WithAccount("seongje")
    public void enrollment_accept_from_MANAGER_APPROVAL_event_when_there_are_no_remains() throws Exception {
        // Given

        // When

        // Then
    }

    @Test
    @DisplayName("관리자 확인 모임 - 모임 신청 마감 전 확정된 인원에 대해 참가 취소 (남은 자리 증가)")
    @WithAccount("seongje")
    public void enrollment_reject_from_MANAGER_APPROVAL_event_before_deadline() throws Exception {
        // Given

        // When

        // Then
    }

    @Test
    @DisplayName("관리자 확인 모임 - 모임 신청 마감 후 확정된 인원에 대해 참가 취소 (변경 없음)")
    @WithAccount("seongje")
    public void enrollment_reject_from_MANAGER_APPROVAL_event_after_deadline() throws Exception {
        // Given

        // When

        // Then
    }

    @Test
    @DisplayName("모임 참가자 출석 체크 - 모임 시작 후 (정상 처리)")
    @WithAccount("seongje")
    public void enrollment_check_in_after_event_start() throws Exception {
        // Given

        // When

        // Then
    }

    @Test
    @DisplayName("모임 참가자 출석 체크 취소 - 모임 시작 후 (정상 처리)")
    @WithAccount("seongje")
    public void enrollment_check_in_cancel_after_event_start() throws Exception {
        // Given

        // When

        // Then
    }
}