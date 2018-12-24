package com.cs.coding.assignment.testutils;

import com.cs.coding.assignment.model.EventDetails;

import static org.springframework.test.util.AssertionErrors.assertEquals;

public class EventDetailsChecker {
    public void eventDetailsEqualsAssertion(String id, Long durationMillis, String type, String host, boolean alert, EventDetails actual) {
        EventDetails expected = new EventDetails(id, durationMillis, type, host, alert);
        assertEquals("Unexpected property values for eventDetails " + id, expected, actual);
    }
}
