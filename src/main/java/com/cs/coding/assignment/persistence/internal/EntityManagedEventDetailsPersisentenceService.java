package com.cs.coding.assignment.persistence.internal;

import com.cs.coding.assignment.model.EventDetails;
import com.cs.coding.assignment.persistence.EventDetailsPersistenceService;
import lombok.NonNull;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
@Scope("singleton")
public class EntityManagedEventDetailsPersisentenceService implements EventDetailsPersistenceService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void persistEventDetails(@NonNull EventDetails eventDetails) {
        entityManager.persist(eventDetails);
    }

    @Override
    public void flush() {
        entityManager.flush();
    }

    @Override
    public List<EventDetails> allEventDetails() {
        // todo other cleaner options might include named queries or using Criteria
        Query query = entityManager.createNativeQuery("select * from EVENT_DETAILS", EventDetails.class);
        return query.getResultList();
    }
}
