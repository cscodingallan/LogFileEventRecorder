package com.cs.coding.assignment.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.test.util.AssertionErrors.assertEquals;

@RunWith(SpringRunner.class)
public class EventDetailsFactoryTest {

    private EventDetailsFactory eventDetailsFactory = new EventDetailsFactory();

    @Test
    public void testConstructionWithValidLogEvents() {
        LogEvent logEvent = new LogEvent("12345", EventState.STARTED.name(), 12345L, null, null);
        LogEvent logEventSibling = new LogEvent("12345", EventState.FINISHED.name(), 12346L, null, null);

        EventDetails eventDetails = eventDetailsFactory.createEventDetailsFromSiblingLogEvents(logEvent, logEventSibling);

        assertEquals("Unexpected id", logEvent.getId(), eventDetails.getId());
        assertEquals("Unexpected duration", 1L, eventDetails.getDurationMillis());
        assertEquals("Unexpected type", null, eventDetails.getType());
        assertEquals("Unexpected host", null, eventDetails.getHost());
        assertEquals("Unexpected alert", false, eventDetails.isAlert());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructionWithLogEventsWithDifferentIds() {
        LogEvent logEvent = new LogEvent("1234", EventState.STARTED.name(), 12345L, null, null);
        LogEvent logEventSibling = new LogEvent("12345", EventState.FINISHED.name(), 12346L, null, null);

        eventDetailsFactory.createEventDetailsFromSiblingLogEvents(logEvent, logEventSibling);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructionWithLogEventsWithSameState() {
        LogEvent logEvent = new LogEvent("12345", EventState.STARTED.name(), 12345L, null, null);
        LogEvent logEventSibling = new LogEvent("12345", EventState.STARTED.name(), 12346L, null, null);

        eventDetailsFactory.createEventDetailsFromSiblingLogEvents(logEvent, logEventSibling);
    }
}
