package com.seongje.studyolle.modules.event.app_event.custom.event;

import com.seongje.studyolle.modules.event.domain.Event;

public class EventCreatedEvent extends EventAppEvent {

    public EventCreatedEvent(Event event) {
        super(event, "새 모임");
    }
}
