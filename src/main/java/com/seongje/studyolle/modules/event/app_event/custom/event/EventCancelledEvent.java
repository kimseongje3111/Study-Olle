package com.seongje.studyolle.modules.event.app_event.custom.event;

import com.seongje.studyolle.modules.event.domain.Event;

public class EventCancelledEvent extends EventAppEvent {

    public EventCancelledEvent(Event event) {
        super(event, "모임 취소");
    }
}
