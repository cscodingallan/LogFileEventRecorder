package com.cs.coding.assignment.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Data entity class representing the event details for store/retrieve from a JPA implementation.
 *
 * JPA and Lombok are not very compatible. JPA wants a default constructor and mutable properties. Lombok @Builder
 * doesn't like the dafault constructor and lombok @Value isn't compatible either because properties must get a
 * value and they don't if there's a default constructor. JPA wins alas, but at least we can make setting props
 * private access as an alternative to @Value.
 */
@Data
@Entity
@Setter(AccessLevel.PRIVATE)
public final class EventDetails {
    @Id
    private @NonNull String id = null;
    private @NonNull Long durationMillis = null;
    private String type = null;
    private String host = null;
    private boolean alert = false;

    // Default constructor needed for JPA / Hibernate
    private EventDetails() {}

    public EventDetails(@NonNull String id, Long durationMillis, String type, String host, boolean alert) {
        this.id = id;
        this.durationMillis = durationMillis;
        this.type = type;
        this.host = host;
        this.alert = alert;
    }
}
