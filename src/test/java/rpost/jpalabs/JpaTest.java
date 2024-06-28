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
        MyEntity entity = new MyEntity();
        Long id = txHelper.doInTransaction(() -> {
            System.err.println(entity);
            System.err.println(entityManager.contains(entity));
            entityManager.persist(entity);
            System.err.println(entity);
            System.err.println(entityManager.contains(entity));
            return entity.getId();
        });

        MyEntity saved = entityManager.find(MyEntity.class, id);
        System.err.println(saved);
    }

    //endregion



    //region first level cache

    // once entity is persistent it's kept in current persistence context (PC)
    // for simplification let's assume PC is just spring transaction
    // this is called jpa first leve cache
    @Test
    void returnsEntityFromPersistenceContextIfAlreadyKnown() {
        MyEntity entity = new MyEntity();
        txHelper.doInTransaction(() -> {
            entityManager.persist(entity);

            MyEntity foundById = entityManager.find(MyEntity.class, entity.getId());
            MyEntity foundByQuery = entityManager.createQuery("from MyEntity", MyEntity.class).getSingleResult();

            Assertions.assertTrue(entity==foundById);
            Assertions.assertTrue(entity==foundByQuery);
        });
    }

    //endregion



    //region cache is always watching you

    // ... this means that if you are processing a lot of data then
    // persistence context might grow unexpectedly big -> boom: OOM
    @Test
    void mightExplodeIfHugeAmountsOfDataIsProcessedWithinSinglePersistenceContext() {
        for (int i = 0; i < 1_000_000; i++) {
            txHelper.doInTransaction(() -> {
                entityManager.persist(new MyEntity());
            });
        }

        entityManager.createQuery("from MyEntity", MyEntity.class)
                .getResultStream()
                .forEach(myEntity -> {
                    // do smth with entity
                });
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
        Long id = txHelper.doInTransaction(() -> {
            MyEntity entity = new MyEntity();
            entityManager.persist(entity);

            entity.setS("hehe");
            return entity.getId();
        });

        MyEntity saved = entityManager.find(MyEntity.class, id);

        Assertions.assertEquals("hehe", saved.getS());
    }

    //endregion



    //region do not persist persistent entity

    // ... this means you don't have to persist second time if you are within transaction!
    @Test
    void youDontHaveToPersistSecondTime() {
        Long id = txHelper.doInTransaction(() -> {
            MyEntity entity = new MyEntity();
            entityManager.persist(entity);

            entity.setS("hehe");
            entityManager.persist(entity); // or repository.save(entity)
            return entity.getId();
        });

        MyEntity saved = entityManager.find(MyEntity.class, id);

        Assertions.assertEquals("hehe", saved.getS());
    }

    //endregion



    //region references

    // in addition to persistent state entity you can also obtain reference
    // which is lazy loaded proxy
    @Test
    void referenceIsLazyLoadedProxy() {
        Long id = txHelper.doInTransaction(() -> {
            MyEntity entity = new MyEntity();
            entityManager.persist(entity);
            return entity.getId();
        });

        PersistenceUnitUtil puu = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();

        txHelper.doInTransaction(() -> {
            MyEntity reference = entityManager.getReference(MyEntity.class, id);
            Assertions.assertEquals(id, reference.getId());
            System.err.println(puu.isLoaded(reference));
            System.err.println(Hibernate.isInitialized(reference));
            System.err.println(entityManager.contains(reference));
//            Assertions.assertEquals(null, reference.getS());
//            System.err.println(puu.isLoaded(reference));
//            System.err.println(Hibernate.isInitialized(reference));
//            System.err.println(entityManager.contains(reference));
        });

//        MyEntity reference = txHelper.doInTransaction(() -> {
//            return entityManager.getReference(MyEntity.class, id);
//        });
//        Assertions.assertEquals(id, reference.getId());
//        Assertions.assertEquals(null, reference.getS());

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
        Long id = txHelper.doInTransaction(() -> {
            MyEntity entity = new MyEntity();
            entityManager.persist(entity);
            return entity.getId();
        });

        txHelper.doInTransaction(() -> {
            MyEntity entity = entityManager.find(MyEntity.class, id);
            entityManager.remove(entity);
            //from this point up to PC close entity is in state removed

            //book say you can cancel this by calling persist :shrug:
        });

        MyEntity notFound = entityManager.find(MyEntity.class, id);
        Assertions.assertNull(notFound);
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
        Long id = txHelper.doInTransaction(() -> {
            MyEntity entity = new MyEntity();
            entityManager.persist(entity);
            entity.setS("hehe");
            return entity.getId();
        });

        txHelper.doInTransaction(() -> {
            entityManager.setFlushMode(FlushModeType.COMMIT);
            MyEntity entity = entityManager.find(MyEntity.class, id);
            entity.setS("changed");

            MyEntity foundByQuery = entityManager.createQuery("from MyEntity", MyEntity.class).getSingleResult();
            // puzzle: what will be result:
            System.err.println(entity.getS());
            System.err.println(foundByQuery.getS());
            System.err.println(entity);
            System.err.println(foundByQuery);

            // puzzle: what if i query for just s instead of whole entity?
        });

        MyEntity foundByQuery = entityManager.createQuery("from MyEntity", MyEntity.class).getSingleResult();
        System.err.println(foundByQuery.getS());
    }

    //endregion


}
//endregion
