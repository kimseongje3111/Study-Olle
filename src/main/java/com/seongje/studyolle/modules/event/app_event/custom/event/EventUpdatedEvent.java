package com.seongje.studyolle.modules.event.app_event.custom.event;

import com.seongje.studyolle.modules.event.domain.Event;

public class EventUpdatedEvent extends EventAppEvent {

    public EventUpdatedEvent(Event event) {
        super(event, "모임 수정");
    }
}
