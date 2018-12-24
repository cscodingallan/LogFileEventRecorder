package com.cs.coding.assignment.testutils;

import com.cs.coding.assignment.model.LogEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class LogEventInputStreamFactory {
    public InputStream createLogEventInputStream(LogEvent[] logEvents) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(logEvents);
            return new ByteArrayInputStream(jsonString.getBytes());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
