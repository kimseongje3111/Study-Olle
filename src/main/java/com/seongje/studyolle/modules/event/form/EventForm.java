package com.seongje.studyolle.modules.event.form;

import com.seongje.studyolle.modules.event.domain.EventType;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

import static com.seongje.studyolle.modules.event.domain.EventType.*;
import static org.springframework.format.annotation.DateTimeFormat.ISO.*;

@Data
public class EventForm {

    @NotBlank
    @Length(max = 50)
    private String title;

    private String description;

    private EventType eventType = FCFS;

    @Min(2)
    private Integer limitOfEnrollments = 2;

    @DateTimeFormat(iso = DATE_TIME)
    private LocalDateTime startDateTime;

    @DateTimeFormat(iso = DATE_TIME)
    private LocalDateTime endDateTime;

    @DateTimeFormat(iso = DATE_TIME)
    private LocalDateTime enrollmentDeadline;
}
