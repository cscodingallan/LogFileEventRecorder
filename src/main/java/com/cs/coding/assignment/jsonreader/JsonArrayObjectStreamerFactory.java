package com.cs.coding.assignment.jsonreader;

import com.cs.coding.assignment.jsonreader.internal.JacksonJsonArraySimpleObjectStreamer;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class JsonArrayObjectStreamerFactory<T> {
    public JsonArrayObjectStreamer<T> createJsonArrayObjectStreamer(InputStream jsonArrayInputStream, TypeReference<T> type) throws IOException {
        return new JacksonJsonArraySimpleObjectStreamer<>(jsonArrayInputStream, type);
    }
}
