package com.cs.coding.assignment.jsonreader;

/**
 * Generic interface for something that streams objects from a JSON array consisting of objects
 * @param <T> the type of object to be streamed
 */
public interface JsonArrayObjectStreamer<T> {
   T next();
}
