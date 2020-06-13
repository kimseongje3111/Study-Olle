package com.seongje.studyolle.modules.event.controller;

import com.seongje.studyolle.infra.AbstractContainerBaseTest;
import com.seongje.studyolle.infra.MockMvcTest;
import com.seongje.studyolle.modules.account.AccountFactory;
import com.seongje.studyolle.modules.account.authentication.WithAccount;
import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.account.repository.AccountRepository;
import com.seongje.studyolle.modules.event.EventFactory;
import com.seongje.studyolle.modules.event.domain.Enrollment;
import com.seongje.studyolle.modules.event.domain.Event;
import com.seongje.studyolle.modules.event.form.EventForm;
import com.seongje.studyolle.modules.event.repository.EnrollmentRepository;
import com.seongje.studyolle.modules.event.repository.EventRepository;
import com.seongje.studyolle.modules.event.service.EventService;
import com.seongje.studyolle.modules.study.StudyFactory;
import com.seongje.studyolle.modules.study.domain.Study;
import com.seongje.studyolle.modules.study.repository.StudyRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static com.seongje.studyolle.modules.event.controller.EventController.*;
import static com.seongje.studyolle.modules.event.domain.EventType.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class EventControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired EventService eventService;
    @Autowired AccountRepository accountRepository;
    @Autowired StudyRepository studyRepository;
    @Autowired EventRepository eventRepository;
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired AccountFactory accountFactory;
    @Autowired StudyFactory studyFactory;
    @Autowired EventFactory eventFactory;
    @Autowired ModelMapper modelMapper;

    @Test
    @DisplayName("모임 수정 - 신청 대기자가 있는 선착순 모임의 모집 인원 변경 (늘어난 자리만큼 선착순으로 대기자 확정)")
    @WithAccount("seongje")
    public void event_update_limit_of_enrollments_form_FCFS_with_waiting() throws Exception {
        // Given
        Account manager = accountRepository.findByNickname("seongje");
        Account guest1 = accountRepository.save(accountFactory.createAccount("guest1"));
        Account guest2 = accountRepository.save(accountFactory.createAccount("guest2"));
        Study testStudy = studyRepository.save(studyFactory.createStudy("test-study", manager));
        Event testEvent = eventFactory.createEvent("test-event", FCFS, 2, testStudy, manager);

        // When 1
        testEvent.setEnrollmentDeadline(LocalDateTime.now().plusDays(5));
        testEvent = eventService.createNewEvent(testEvent, manager, testStudy);
        eventService.enrollToEvent(testEvent, manager);
        eventService.enrollToEvent(testEvent, guest1);
        eventService.enrollToEvent(testEvent, guest2);

        // Then 1
        isAccepted(manager, testEvent);
        isAccepted(guest1, testEvent);
        isNotAccepted(guest2, testEvent);

        // When 2
        EventForm eventForm = modelMapper.map(testEvent, EventForm.class);
        eventForm.setLimitOfEnrollments(3);
        eventService.updateEvent(testEvent, eventForm);

        // Then 2
        isAccepted(guest2, testEvent);
    }

    @Test
    @DisplayName("모임 수정 - 확정된 인원보다 적게 모집 인원 변경 (변경 불가)")
    @WithAccount("seongje")
    public void event_update_limit_of_enrollments_less_than_number_of_accepted() throws Exception {
        // Given
        Account manager = accountRepository.findByNickname("seongje");
        Account guest1 = accountRepository.save(accountFactory.createAccount("guest1"));
        Account guest2 = accountRepository.save(accountFactory.createAccount("guest2"));
        Study testStudy = studyRepository.save(studyFactory.createStudy("test-study", manager));
        Event testEvent = eventFactory.createEvent("test-event", FCFS, 3, testStudy, manager);

        // When
        testEvent.setEnrollmentDeadline(LocalDateTime.now().plusDays(5));
        testEvent = eventService.createNewEvent(testEvent, manager, testStudy);
        eventService.enrollToEvent(testEvent, manager);
        eventService.enrollToEvent(testEvent, guest1);
        eventService.enrollToEvent(testEvent, guest2);

        EventForm eventForm = modelMapper.map(testEvent, EventForm.class);
        eventForm.setLimitOfEnrollments(2);
        eventService.updateEvent(testEvent, eventForm);

        // Then
        assertThat(testEvent.getLimitOfEnrollments()).isEqualTo(3);
    }


    @Test
    @DisplayName("모임 참가 신청 - 신청 마감 전, 모집 인원 내 (자동 확정)")
    @WithAccount("seongje")
    public void enrollment_before_deadline() throws Exception {
        // Given
        Account manager = accountRepository.save(accountFactory.createAccount("manager"));
        Study testStudy = studyRepository.save(studyFactory.createStudy("test-study", manager));
        Event testEvent = eventFactory.createEvent("test-event", FCFS, 2, testStudy, manager);

        // When
        testEvent.setEnrollmentDeadline(LocalDateTime.now().plusDays(5));
        testEvent = eventService.createNewEvent(testEvent, manager, testStudy);

        mockMvc.perform(post("/study/studies/" + testStudy.getEncodedPath()
                + "/event/events/" + testEvent.getId() + "/enroll").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/studies/" + testStudy.getEncodedPath()
                        + "/event/events/" + testEvent.getId()));

        // Then
        Account guest = accountRepository.findByNickname("seongje");
        isAccepted(guest, testEvent);
    }

    @Test
    @DisplayName("모임 참가 취소 - 신청 마감 전 (정상 처리)")
    @WithAccount("seongje")
    public void enrollment_cancel_before_deadline() throws Exception {
        // Given
        Account manager = accountRepository.save(accountFactory.createAccount("manager"));
        Account guest = accountRepository.findByNickname("seongje");
        Study testStudy = studyRepository.save(studyFactory.createStudy("test-study", manager));
        Event testEvent = eventFactory.createEvent("test-event", FCFS, 2, testStudy, manager);

        // When
        testEvent.setEnrollmentDeadline(LocalDateTime.now().plusDays(5));
        testEvent = eventService.createNewEvent(testEvent, manager, testStudy);
        eventService.enrollToEvent(testEvent, manager);
        eventService.enrollToEvent(testEvent, guest);

        mockMvc.perform(post("/study/studies/" + testStudy.getEncodedPath()
                + "/event/events/" + testEvent.getId() + "/disenroll").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/studies/" + testStudy.getEncodedPath()
                        + "/event/events/" + testEvent.getId()));

        // Then
        Enrollment findEnrollment = enrollmentRepository.searchEnrollmentByEventAndAccount(testEvent.getId(), guest.getId());
        assertThat(findEnrollment).isNull();
        assertThat(testEvent.getEnrollments().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("모임 참가 신청 - 신청 마감 후 (변경 없음)")
    @WithAccount("seongje")
    public void enrollment_after_deadline() throws Exception {
        // Given
        Account manager = accountRepository.save(accountFactory.createAccount("manager"));
        Study testStudy = studyRepository.save(studyFactory.createStudy("test-study", manager));
        Event testEvent = eventFactory.createEvent("test-event", FCFS, 2, testStudy, manager);

        // When
        testEvent.setEnrollmentDeadline(LocalDateTime.now().minusDays(5));
        testEvent = eventService.createNewEvent(testEvent, manager, testStudy);

        mockMvc.perform(post("/study/studies/" + testStudy.getEncodedPath()
                + "/event/events/" + testEvent.getId() + "/enroll").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/studies/" + testStudy.getEncodedPath()
                        + "/event/events/" + testEvent.getId()));

        // Then
        Account guest = accountRepository.findByNickname("seongje");
        Enrollment findEnrollment = enrollmentRepository.searchEnrollmentByEventAndAccount(testEvent.getId(), guest.getId());
        assertThat(findEnrollment).isNull();
    }

    @Test
    @DisplayName("모임 참가 취소 - 신청 마감 후 (변경 없음)")
    @WithAccount("seongje")
    public void enrollment_cancel_after_deadline() throws Exception {
        // Given
        Account manager = accountRepository.save(accountFactory.createAccount("manager"));
        Account guest = accountRepository.findByNickname("seongje");
        Study testStudy = studyRepository.save(studyFactory.createStudy("test-study", manager));
        Event testEvent = eventFactory.createEvent("test-event", FCFS, 2, testStudy, manager);

        // When
        testEvent.setEnrollmentDeadline(LocalDateTime.now().plusDays(5));
        testEvent = eventService.createNewEvent(testEvent, manager, testStudy);
        eventService.enrollToEvent(testEvent, manager);
        eventService.enrollToEvent(testEvent, guest);

        EventForm eventForm = modelMapper.map(testEvent, EventForm.class);
        eventForm.setEnrollmentDeadline(LocalDateTime.now().minusDays(5));
        eventService.updateEvent(testEvent, eventForm);

        mockMvc.perform(post("/study/studies/" + testStudy.getEncodedPath()
                + "/event/events/" + testEvent.getId() + "/disenroll").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/studies/" + testStudy.getEncodedPath()
                        + "/event/events/" + testEvent.getId()));

        // Then
        Enrollment findEnrollment = enrollmentRepository.searchEnrollmentByEventAndAccount(testEvent.getId(), guest.getId());
        assertThat(findEnrollment).isNotNull();
        assertThat(testEvent.getNumberOfAcceptedEnrollments()).isEqualTo(2);
    }

    @Test
    @DisplayName("선착순 모임 - 모집 인원 내 모임 신청 (자동 확정)")
    @WithAccount("seongje")
    public void enrollment_to_FCFS_event_when_there_are_remains() throws Exception {
        // Given
        Account manager = accountRepository.save(accountFactory.createAccount("manager"));
        Study testStudy = studyRepository.save(studyFactory.createStudy("test-study", manager));
        Event testEvent = eventFactory.createEvent("test-event", FCFS, 2, testStudy, manager);

        // When
        testEvent.setEnrollmentDeadline(LocalDateTime.now().plusDays(5));
        testEvent = eventService.createNewEvent(testEvent, manager, testStudy);
        eventService.enrollToEvent(testEvent, manager);

        mockMvc.perform(post("/study/studies/" + testStudy.getEncodedPath()
                + "/event/events/" + testEvent.getId() + "/enroll").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/studies/" + testStudy.getEncodedPath()
                        + "/event/events/" + testEvent.getId()));

        // Then
        Account guest = accountRepository.findByNickname("seongje");
        isAccepted(guest, testEvent);
        assertThat(testEvent.getNumberOfAcceptedEnrollments()).isEqualTo(2);
    }

    @Test
    @DisplayName("선착순 모임 - 모집 인원 초과시 모임 신청 (대기 중)")
    @WithAccount("seongje")
    public void enrollment_to_FCFS_event_when_there_are_no_remains() throws Exception {
        // Given
        Account manager = accountRepository.save(accountFactory.createAccount("manager"));
        Account guest1 = accountRepository.save(accountFactory.createAccount("guest1"));
        Study testStudy = studyRepository.save(studyFactory.createStudy("test-study", manager));
        Event testEvent = eventFactory.createEvent("test-event", FCFS, 2, testStudy, manager);

        // When
        testEvent.setEnrollmentDeadline(LocalDateTime.now().plusDays(5));
        testEvent = eventService.createNewEvent(testEvent, manager, testStudy);
        eventService.enrollToEvent(testEvent, manager);
        eventService.enrollToEvent(testEvent, guest1);

        mockMvc.perform(post("/study/studies/" + testStudy.getEncodedPath()
                + "/event/events/" + testEvent.getId() + "/enroll").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/studies/" + testStudy.getEncodedPath()
                        + "/event/events/" + testEvent.getId()));

        // Then
        Account guest2 = accountRepository.findByNickname("seongje");
        isNotAccepted(guest2, testEvent);
        assertThat(testEvent.getEnrollments().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("선착순 모임 - 모집 인원 초과시 취소자 발생, 대기자 없음 (남은 자리 증가)")
    @WithAccount("seongje")
    public void enrollment_cancel_from_FCFS_event_when_there_is_no_waiting() throws Exception {
        // Given
        Account manager = accountRepository.save(accountFactory.createAccount("manager"));
        Account guest = accountRepository.findByNickname("seongje");
        Study testStudy = studyRepository.save(studyFactory.createStudy("test-study", manager));
        Event testEvent = eventFactory.createEvent("test-event", FCFS, 2, testStudy, manager);

        // When 1
        testEvent.setEnrollmentDeadline(LocalDateTime.now().plusDays(5));
        testEvent = eventService.createNewEvent(testEvent, manager, testStudy);
        eventService.enrollToEvent(testEvent, manager);
        eventService.enrollToEvent(testEvent, guest);

        // Then 1
        assertThat(testEvent.getNumberOfRemainingSeats()).isEqualTo(0);

        // When 2
        mockMvc.perform(post("/study/studies/" + testStudy.getEncodedPath()
                + "/event/events/" + testEvent.getId() + "/disenroll").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/studies/" + testStudy.getEncodedPath()
                        + "/event/events/" + testEvent.getId()));

        // Then 2
        Enrollment findEnrollment = enrollmentRepository.searchEnrollmentByEventAndAccount(testEvent.getId(), guest.getId());
        assertThat(findEnrollment).isNull();
        assertThat(testEvent.getNumberOfRemainingSeats()).isEqualTo(1);
    }

    @Test
    @DisplayName("선착순 모임 - 모집 인원 초과시 취소자 발생, 대기자 있음 (취소자 수만큼 선착순으로 대기자 확정)")
    @WithAccount("seongje")
    public void enrollment_cancel_from_FCFS_event_when_there_is_waiting() throws Exception {
        // Given
        Account manager = accountRepository.save(accountFactory.createAccount("manager"));
        Account guest1 = accountRepository.findByNickname("seongje");
        Account guest2 = accountRepository.save(accountFactory.createAccount("guest2"));
        Study testStudy = studyRepository.save(studyFactory.createStudy("test-study", manager));
        Event testEvent = eventFactory.createEvent("test-event", FCFS, 2, testStudy, manager);

        // When
        testEvent.setEnrollmentDeadline(LocalDateTime.now().plusDays(5));
        testEvent = eventService.createNewEvent(testEvent, manager, testStudy);
        eventService.enrollToEvent(testEvent, manager);
        eventService.enrollToEvent(testEvent, guest1);
        eventService.enrollToEvent(testEvent, guest2);

        mockMvc.perform(post("/study/studies/" + testStudy.getEncodedPath()
                + "/event/events/" + testEvent.getId() + "/disenroll").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/studies/" + testStudy.getEncodedPath()
                        + "/event/events/" + testEvent.getId()));

        // Then
        isAccepted(guest2, testEvent);
    }

    @Test
    @DisplayName("관리자 확인 모임 - 모임 신청 (대기 중)")
    @WithAccount("seongje")
    public void enrollment_to_MANAGER_APPROVAL_event_when_there_are_remains() throws Exception {
        // Given
        Account manager = accountRepository.save(accountFactory.createAccount("manager"));
        Study testStudy = studyRepository.save(studyFactory.createStudy("test-study", manager));
        Event testEvent = eventFactory.createEvent("test-event", MANAGER_APPROVAL, 5, testStudy, manager);

        // When
        testEvent.setEnrollmentDeadline(LocalDateTime.now().plusDays(5));
        testEvent = eventService.createNewEvent(testEvent, manager, testStudy);
        eventService.enrollToEvent(testEvent, manager);

        mockMvc.perform(post("/study/studies/" + testStudy.getEncodedPath()
                + "/event/events/" + testEvent.getId() + "/enroll").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/studies/" + testStudy.getEncodedPath()
                        + "/event/events/" + testEvent.getId()));

        // Then
        Account guest = accountRepository.findByNickname("seongje");
        isNotAccepted(guest, testEvent);
        assertThat(testEvent.getNumberOfAcceptedEnrollments()).isEqualTo(1);
    }

    private void isAccepted(Account account, Event event) {
        Enrollment findEnrollment = enrollmentRepository.searchEnrollmentByEventAndAccount(event.getId(), account.getId());
        assertTrue(findEnrollment.isApproved());
    }

    private void isNotAccepted(Account account, Event event) {
        Enrollment findEnrollment = enrollmentRepository.searchEnrollmentByEventAndAccount(event.getId(), account.getId());
        assertFalse(findEnrollment.isApproved());
    }
}