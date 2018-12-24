package com.cs.coding.assignment.service;

import com.cs.coding.assignment.model.EventDetails;
import com.cs.coding.assignment.persistence.EventDetailsPersistenceService;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

/**
 * The role of LogFileEventRecorder is to utilize an EventDetails Observable and to invoke a EventDetailsPersistenceService to persist
 * observed EventDetails.
 */
@Service
@Slf4j
public class LogFileEventRecorder {

    private EventDetailsPersistenceService eventDetailsPersistenceService;
    private EventDetailsObservableFactory eventDetailsObservableFactory;

    public LogFileEventRecorder(EventDetailsPersistenceService eventDetailsPersistenceService, EventDetailsObservableFactory eventDetailsObservableFactory) {
        this.eventDetailsPersistenceService = eventDetailsPersistenceService;
        this.eventDetailsObservableFactory = eventDetailsObservableFactory;
    }

    public void recordLogFileEvents(InputStream jsonLogFileEventInputStream) throws IOException {
        Observable<EventDetails> eventDetailsObservable = eventDetailsObservableFactory.createEventDetailsObservable(jsonLogFileEventInputStream);
        eventDetailsObservable
                .flatMap(value -> Observable.just(value)
                        .subscribeOn(Schedulers.io())
                        .doOnNext(eventDetails -> eventDetailsPersistenceService.persistEventDetails(eventDetails)))
                .doOnError(e -> log.error(e.getMessage(), e))
                .doOnComplete(() -> eventDetailsPersistenceService.flush())
                .subscribe();
    }
}

