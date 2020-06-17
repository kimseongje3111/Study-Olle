package com.seongje.studyolle.modules.event.app_event.custom.event;

import com.seongje.studyolle.modules.event.domain.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter @Setter
@RequiredArgsConstructor
public abstract class EventAppEvent {

    private final Event event;
    private final String messageTitle;
    private String messageContent;

    public String getMessage() {
        return "[" + messageTitle + "] " + messageContent;
    }
}
