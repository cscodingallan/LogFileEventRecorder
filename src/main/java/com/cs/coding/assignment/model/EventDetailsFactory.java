package com.cs.coding.assignment.model;

import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class EventDetailsFactory {
    /**
     * Create new EventDetails instances from sibling LogEvents.
     * @throws IllegalArgumentException - the two LogEvents must have the same id and different states; otherwise an IllegalArgumentException is thrown
     */
    public @NonNull EventDetails createEventDetailsFromSiblingLogEvents(@NonNull LogEvent logEvent, @NonNull LogEvent logEventSibling) throws IllegalArgumentException {
        this.verifyLogEventsAreSiblings(logEvent, logEventSibling);
        Long durationMillis = Math.abs(logEvent.getTimestampMillis() - logEventSibling.getTimestampMillis());
        return new EventDetails(logEvent.getId(),
                durationMillis,
                logEvent.getType(),
                logEvent.getHost(), durationMillis > 4);
    }

    private void verifyLogEventsAreSiblings(@NonNull LogEvent logEvent, @NonNull LogEvent logEventSibling) {
        boolean idsDifferent = !logEvent.getId().equals(logEventSibling.getId());
        boolean statesEqual = logEvent.getState() == logEventSibling.getState();

        if (idsDifferent || statesEqual) {
            throw new IllegalArgumentException("Tried to create an EventDetails with LogEvents that are not siblings: " + logEvent + ", " + logEventSibling);
        }
    }
}
