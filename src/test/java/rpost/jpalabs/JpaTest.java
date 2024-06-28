// welcome!

//region organization

// short sessions weekly/biweekly
// loosely based on https://www.manning.com/books/java-persistence-with-spring-data-and-hibernate
// cooperation welcome!

//endregion

//region rules

// one rule: do not be surprised when your colleague doesn't know something
// second rule: we ask questions and discuss

//endregion

//region tell me your expectations

// who is using jpa?
// who is new to jpa?
// who knows sql?

//endregion

//region topic for today

// how to save data to database with jpa
// aka: entity lifecycle
// (chapter 10 from book)

//endregion

//region let's start!
package rpost.jpalabs;

import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.PersistenceUnitUtil;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Import(TxHelper.class)
@DataJpaTest(showSql = false)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class JpaTest {

    @Autowired
    TxHelper txHelper;

    @Autowired
    EntityManager entityManager;



    //region persisting
    @Test
    void persistMakesTransientEntityPersistentAkaManaged() {
    }

    //endregion



    //region first level cache

    // once entity is persistent it's kept in current persistence context (PC)
    // for simplification let's assume PC is just spring transaction
    // this is called jpa first leve cache
    @Test
    void returnsEntityFromPersistenceContextIfAlreadyKnown() {
    }

    //endregion



    //region cache is always watching you

    // ... this means that if you are processing a lot of data then
    // persistence context might grow unexpectedly big -> boom: OOM
    @Test
    void mightExplodeIfHugeAmountsOfDataIsProcessedWithinSinglePersistenceContext() {
    }

    // solution for this is
    // EntityManager.close (detaches all entities in PC) or
    // EntityManager.unwrap(Session.class).setDefaultReadOnly(true) (disables synchronization of changes to DB)
    // just using batches

    //endregion



    //region managed entity is well... managed

    // persistent entity is tracked by hibernate for changes
    // they are automatically sent to database
    @Test
    void synchronizesPersistentEntityWithDatabase() {
    }

    //endregion



    //region do not persist persistent entity

    // ... this means you don't have to persist second time if you are within transaction!
    @Test
    void youDontHaveToPersistSecondTime() {
    }

    //endregion



    //region references

    // in addition to persistent state entity you can also obtain reference
    // which is lazy loaded proxy
    @Test
    void referenceIsLazyLoadedProxy() {
    }
    //endregion



    //region puzzle

    // what happens if we try to obtain reference to entity known to PC?

    //region answer

    // we get initialized persistent entity

    //endregion

    //endregion



    //region puzzle

    // what happens if we load reference that doesn't exist in DB?

    //region answer
    // we get proxy of which any initialization will cause EntityNotFoundException
    //endregion

    //endregion



    //region puzzle

    // what happens if we obtain reference and then load full persistent entity

    //region answer
    // i don't know but we can check
    //endregion

    //endregion



    //region removed state
    @Test
    void removeRemoves() {
    }
    //endregion



    //region refresh

    // EntityManager contains refresh operation which overrides persistent entity with fresh data from DB
    // Q to audience: do we want to see this in action?
    // Expected answer: no
    // Just use optimistic locking instead and voila

    //endregion



    //region detached state

    // Persistent entity can get into detached state when:
    // - PC is closed or
    // - EntityManager.detach is called
    // such entity is no longer managed by PC

    //endregion



    //region merging detached state

    // Operation opposite to detach is merge:
    // it takes detached (or transient!) and makes it persistent (ie: insert or update)

    //endregion



    //region flushing

    // Flushing by default occurs:
    // - when transaction ends or
    // - before every query or
    // - on demand (EntityManager.flush)
    @Test
    void youCanControlFlushing() {
    }

    //endregion


}
//endregion
