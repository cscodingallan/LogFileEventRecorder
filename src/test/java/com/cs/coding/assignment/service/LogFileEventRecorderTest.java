package com.cs.coding.assignment.service;

import com.cs.coding.assignment.model.EventDetails;
import com.cs.coding.assignment.model.EventState;
import com.cs.coding.assignment.model.LogEvent;
import com.cs.coding.assignment.persistence.EventDetailsPersistenceService;
import com.cs.coding.assignment.testutils.EventDetailsChecker;
import com.cs.coding.assignment.testutils.LogEventInputStreamFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.fail;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest // todo this works but invokes the main app - find a way to autowire in unit tests without having to use xml
public class LogFileEventRecorderTest {

    private LogEventInputStreamFactory logEventInputStreamFactory = new LogEventInputStreamFactory();

    @Autowired
    private EventDetailsPersistenceService eventDetailsPersistenceService;

    @Autowired
    private LogFileEventRecorder logFileEventRecorder;

    @Test
    public void testRecordingOfSomeLogEvents() throws IOException {

        LogEvent logEvent1 = new LogEvent("1", EventState.STARTED.name(), 1L,"APPLICATION_LOG", "localhost");
        LogEvent logEvent2 = new LogEvent("2", EventState.FINISHED.name(), 8L,"APPLICATION_LOG", "localhost");
        LogEvent logEvent3 = new LogEvent("3", EventState.FINISHED.name(), 7L,null, null);
        LogEvent logEvent4 = new LogEvent("2", EventState.STARTED.name(), 3L,"APPLICATION_LOG", "localhost");
        LogEvent logEvent5 = new LogEvent("1", EventState.FINISHED.name(), 2L,"APPLICATION_LOG", "localhost");
        LogEvent logEvent6 = new LogEvent("3", EventState.STARTED.name(), 2L,null, null);

        LogEvent [] logEvents = { logEvent1, logEvent2, logEvent3, logEvent4, logEvent5, logEvent6 };

        try (InputStream jsonInputStream = logEventInputStreamFactory.createLogEventInputStream(logEvents)) {
            logFileEventRecorder.recordLogFileEvents(jsonInputStream);
        }
        EventDetailsChecker eventDetailsChecker = new EventDetailsChecker();
        List<EventDetails> allEventDetails = eventDetailsPersistenceService.allEventDetails();
        assertEquals ("Unexpected eventDetails count", 3, allEventDetails.size());
        for (EventDetails eventDetails : allEventDetails) {
            switch (eventDetails.getId()) {
                case "1":
                    eventDetailsChecker.eventDetailsEqualsAssertion("1", 1L, "APPLICATION_LOG", "localhost", false, eventDetails);
                    break;
                case "2":
                    eventDetailsChecker.eventDetailsEqualsAssertion("2", 5L, "APPLICATION_LOG", "localhost", true, eventDetails);
                    break;
                case "3":
                    eventDetailsChecker.eventDetailsEqualsAssertion("3", 5L, null, null, true, eventDetails);
                    break;
                default:
                    fail("Unexpected eventDetails id: " + eventDetails.getId());
            }
        }
    }
}
