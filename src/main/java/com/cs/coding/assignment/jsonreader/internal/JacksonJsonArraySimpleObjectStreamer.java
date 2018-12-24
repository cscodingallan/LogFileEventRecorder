package com.cs.coding.assignment.jsonreader.internal;

import com.cs.coding.assignment.jsonreader.JsonArrayObjectStreamer;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * A JsonArrayObjectStream uses Jackson to stream simple json objects that are embedded within an array. "Simple" means
 * object comprised only of primitives: string, boolean, int, float.
 *
 * This approach to parsing JSON removes the need to store the entire JSON document in memory.
 *
 * @param <T> Any Object comprised only of primitives that is expected to be represented by JSON in the provided
 *            InputStream within a JSON array
 */
@Slf4j
public class JacksonJsonArraySimpleObjectStreamer<T> implements JsonArrayObjectStreamer<T> {

    private @NonNull JsonParser jsonParser;
    private @NonNull TypeReference<T> typeReference;

    private ParseState parseState = ParseState.UNDEFINED;

    /**
     * @param jsonSource - JSON input stream consisting of a single JSON array containing objects that are to be mapped
     *                   to instances of type T
     * @param typeReference - the type reference of T to inform Jackson about the type of object that it is expected
     *                      to build
     * @throws IOException - if there's a problem making the json parson, then Jackson will throw this IOException
     */
    public JacksonJsonArraySimpleObjectStreamer(@NonNull InputStream jsonSource, @NonNull TypeReference<T> typeReference) throws IOException {

        JsonFactory jfactory = new JsonFactory();
        this.jsonParser = jfactory.createParser(jsonSource);
        this.typeReference = typeReference;
    }

    @Override
    public T next() {

        StringBuilder jsonObjectStringBuilder = new StringBuilder();
        JsonToken nextToken;

        try {

            do {
                nextToken = jsonParser.nextToken();

                switch (nextToken) {
                    case START_ARRAY:
                        changeParseState(ParseState.ARRAY_STARTED);
                        break;
                    case START_OBJECT:
                        changeParseState(ParseState.OBJECT_STARTED);
                        jsonObjectStringBuilder.append('{');
                        break;
                    case FIELD_NAME:
                        if (!isFirstJsonProperty(jsonObjectStringBuilder.toString())) {
                            jsonObjectStringBuilder.append(',');
                        }
                        jsonObjectStringBuilder.append('"').append(jsonParser.currentName()).append('"');
                        break;
                    case VALUE_STRING:
                        jsonObjectStringBuilder.append(':').append('"').append(jsonParser.getValueAsString()).append('"');
                        break;
                    case VALUE_FALSE:
                    case VALUE_TRUE:
                    case VALUE_NUMBER_FLOAT:
                    case VALUE_NUMBER_INT:
                    case VALUE_NULL:
                        jsonObjectStringBuilder.append(':').append(jsonParser.getValueAsString());
                        break;
                    case END_OBJECT:
                        changeParseState(ParseState.OBJECT_ENDED);
                        jsonObjectStringBuilder.append('}');
                        break;
                    case END_ARRAY:
                        changeParseState(ParseState.ARRAY_ENDED);
                        jsonParser.close();
                        break;
                }
            } while (nextToken != JsonToken.END_OBJECT && nextToken != JsonToken.END_ARRAY);

            if (nextToken == JsonToken.END_OBJECT) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(jsonObjectStringBuilder.toString(), typeReference);
            }

        } catch (IOException | IllegalStateException e) {
            log.error(e.getMessage(), e);
            try {
                jsonParser.close();
            } catch (IOException e1) {
                log.error(e.getMessage(), e);
            }
        }

        return null;
    }

    private boolean isFirstJsonProperty(String jsonString) {
        return !jsonString.contains(":");
    }

    private void changeParseState(ParseState newParseState) throws IllegalStateException {

        if (parseState.idValidNextState(newParseState)) {
            parseState = newParseState;
        } else {
            throw new IllegalStateException("Tried to change state from " + parseState + " to " + newParseState);
        }
    }

    private enum ParseState {
        ARRAY_ENDED,
        OBJECT_ENDED,
        OBJECT_STARTED,
        ARRAY_STARTED,
        UNDEFINED;

        boolean idValidNextState(ParseState nextStateCandidate) {
            switch (this) {
                case UNDEFINED:
                    return nextStateCandidate == ARRAY_STARTED;
                case ARRAY_STARTED:
                    return nextStateCandidate == OBJECT_STARTED;
                case OBJECT_STARTED:
                    return nextStateCandidate == OBJECT_ENDED;
                case OBJECT_ENDED:
                    return nextStateCandidate == OBJECT_STARTED || nextStateCandidate == ARRAY_ENDED;
                case ARRAY_ENDED:
                    return nextStateCandidate == UNDEFINED;
                default:
                    return false;
            }
        }
    }
}



