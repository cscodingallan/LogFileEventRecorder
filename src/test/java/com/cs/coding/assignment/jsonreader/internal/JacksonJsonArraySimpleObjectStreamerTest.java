package com.cs.coding.assignment.jsonreader.internal;

import com.cs.coding.assignment.jsonreader.JsonArrayObjectStreamer;
import com.cs.coding.assignment.model.EventState;
import com.cs.coding.assignment.model.LogEvent;
import com.cs.coding.assignment.testutils.LogEventInputStreamFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.tools.javac.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@RunWith(SpringRunner.class)
public class JacksonJsonArraySimpleObjectStreamerTest {

    private LogEventInputStreamFactory logEventInputStreamFactory = new LogEventInputStreamFactory();

    @Test
    public void testSingleLogEvent() throws IOException {
        LogEvent logEvent = new LogEvent("123456", EventState.STARTED.name(), 123456221413417L,"APPLICATION_LOG", "localhost");

        InputStream jsonInputStream = logEventInputStreamFactory.createLogEventInputStream(List.of(logEvent).toArray(new LogEvent[]{}));

        JsonArrayObjectStreamer<LogEvent> jsonArrayObjectStreamer = new JacksonJsonArraySimpleObjectStreamer<>(jsonInputStream, new TypeReference<LogEvent>() {});
        LogEvent next = jsonArrayObjectStreamer.next();

        assert(next.equals(logEvent));
    }

    @Test
    public void testMultipleLogEvents() throws IOException {
        LogEvent logEvent1 = new LogEvent("123456", EventState.STARTED.name(), 12341413413567L,"APPLICATION_LOG", "localhost");
        LogEvent logEvent2 = new LogEvent("123456", EventState.FINISHED.name(), 12341941341352267L,"APPLICATION_LOG", "localhost");
        LogEvent logEvent3 = new LogEvent("123456221", EventState.FINISHED.name(), 123414133567L,null, null);

        InputStream jsonInputStream = logEventInputStreamFactory.createLogEventInputStream(List.of(logEvent1, logEvent2, logEvent3).toArray(new LogEvent[]{}));

        JsonArrayObjectStreamer<LogEvent> jsonArrayObjectStreamer = new JacksonJsonArraySimpleObjectStreamer<>(jsonInputStream, new TypeReference<LogEvent>() {});

        assert(jsonArrayObjectStreamer.next().equals(logEvent1));
        assert(jsonArrayObjectStreamer.next().equals(logEvent2));
        assert(jsonArrayObjectStreamer.next().equals(logEvent3));
    }

    @Test
    public void testInvalidJson() throws IOException {

        InputStream jsonInputStream = new ByteArrayInputStream("invalid json".getBytes());

        JsonArrayObjectStreamer<LogEvent> jsonArrayObjectStreamer = new JacksonJsonArraySimpleObjectStreamer<>(jsonInputStream, new TypeReference<LogEvent>() {});

        assert(jsonArrayObjectStreamer.next() == null);
    }
}
