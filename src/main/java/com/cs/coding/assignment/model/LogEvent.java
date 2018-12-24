package com.cs.coding.assignment.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;

/**
 * Data class representing the JSON log events
 */
@Value
public final class LogEvent {
    private @NonNull String id;
    private @NonNull EventState state;
    private @NonNull Long timestampMillis;
    private String type;
    private String host;

    @JsonCreator
    public LogEvent(
            @JsonProperty("id") String id,
            @JsonProperty("state") String state,
            @JsonProperty("timestamp") Long timestampMillis,
            @JsonProperty("type") String type,
            @JsonProperty("host") String host) {
        this.id = id;
        this.timestampMillis = timestampMillis;
        this.state = EventState.valueOf(state.toUpperCase());
        this.type = type;
        this.host = host;
    }
}
