package com.cs.coding.assignment.service;

import com.cs.coding.assignment.jsonreader.JsonArrayObjectStreamer;
import com.cs.coding.assignment.jsonreader.JsonArrayObjectStreamerFactory;
import com.cs.coding.assignment.model.EventDetails;
import com.cs.coding.assignment.model.EventDetailsFactory;
import com.cs.coding.assignment.model.LogEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * Factory whose role is to create Observables that emit EventDetails. The purpose of these observables is to
 * read json based LogEvents from a provided InputStream and if a sibling of the read json LogEvent has already been
 * enountered (i.e. where a sibling is another LogEvent with the same id but it would have a different state) then use
 * both LogEvents to make an EventDetails and emit that. Otherwise just add the LogEvent to an in memory store until its
 * sibling arrives.
 */
@Component
public class EventDetailsObservableFactory {

    private EventDetailsFactory eventDetailsFactory;
    private JsonArrayObjectStreamerFactory<LogEvent> jsonArrayObjectStreamerFactory;

    public EventDetailsObservableFactory(JsonArrayObjectStreamerFactory<LogEvent> jsonArrayObjectStreamerFactory, EventDetailsFactory eventDetailsFactory) {
        this.jsonArrayObjectStreamerFactory = jsonArrayObjectStreamerFactory;
        this.eventDetailsFactory = eventDetailsFactory;
    }

    Observable<EventDetails> createEventDetailsObservable(InputStream logEventsJsonInputStream) throws IOException {
        JsonArrayObjectStreamer<LogEvent> jsonArrayObjectStreamer = jsonArrayObjectStreamerFactory.createJsonArrayObjectStreamer(logEventsJsonInputStream, new TypeReference<LogEvent>() {});
        return Observable.create((ObservableEmitter<EventDetails> eventDetailsEmitter) -> {

            Map<String, LogEvent> logEvents = new HashMap<>();
            LogEvent nextLogEvent = jsonArrayObjectStreamer.next();
            while (nextLogEvent != null) {

                LogEvent siblingLogEvent = logEvents.get(nextLogEvent.getId());
                if (siblingLogEvent != null) {
                    logEvents.remove(siblingLogEvent.getId());
                    try {
                        EventDetails eventDetails = eventDetailsFactory.createEventDetailsFromSiblingLogEvents(nextLogEvent, siblingLogEvent);
                        eventDetailsEmitter.onNext(eventDetails);
                    } catch (IllegalArgumentException e) {
                        eventDetailsEmitter.onError(e);
                    }
                } else {
                    logEvents.put(nextLogEvent.getId(), nextLogEvent);
                }
                nextLogEvent = jsonArrayObjectStreamer.next();
            }
            eventDetailsEmitter.onComplete();
        });
    }
}
