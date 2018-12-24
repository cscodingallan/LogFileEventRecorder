package com.cs.coding.assignment.persistence;

import com.cs.coding.assignment.model.EventDetails;
import lombok.NonNull;

import java.util.List;

public interface EventDetailsPersistenceService {
    void persistEventDetails(@NonNull EventDetails eventDetails);
    void flush();

    // Provide a way to get all stored EventDetails for test purposes
    List<EventDetails> allEventDetails();
}
