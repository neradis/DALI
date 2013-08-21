package de.uni_leipzig.mack.persistence

import aksw.org.doodle.dataset.Description
import aksw.org.doodle.dataset.VectorDescription
import aksw.org.doodle.engine.Engine
import com.db4o.ObjectContainer as OC
import com.db4o.query.Predicate
import com.db4o.ta.Activatable
import com.google.common.base.Stopwatch
import com.google.common.collect.HashMultiset
import de.uni_leipzig.mack.config.Environment
import de.uni_leipzig.mack.evaluation.KnowledgeBase
import de.uni_leipzig.mack.persistence.testing.SimpleLinkedList
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Log4j
import groovyx.gpars.GParsPool

import static java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@TypeChecked
@Log4j('logger')
class Db4oTest extends GroovyTestCase {

    void testTransactionWithBulkOperations() {
        def sw = new Stopwatch().start()
        Engine.instance.datasets
        sw.stop()
        logger.info "Reading void stats from files took ${sw.elapsed(MILLISECONDS)} ms"


        10.times {
            sw.reset().start()
            db4o.writeTransaction { OC oc ->
                def kbObjects = oc.query(KnowledgeBase.class)

                for (kb in kbObjects) {
                    oc.delete(kb)
                }
            }
            sw.stop()
            logger.info "Deleting KBs took ${sw.elapsed(MILLISECONDS)} ms"

            db4o.readOnlyOperation { OC oc ->
                def kbObjects = oc.query(KnowledgeBase.class)
                if (kbObjects.size() > 0) {
                    logger.warn "Db should have deleted all KB objects, but ${kbObjects.size()} kbs still present"
                }
            }

            sw.reset().start()
            GParsPool.withPool {
                Engine.instance.datasets.entrySet().eachParallel { Map.Entry<String, Description> e ->
                    db4o.writeTransaction { OC oc ->
                        def desc = e.value
                        if (desc instanceof VectorDescription) {
                            oc.store(new KnowledgeBase(e.key, desc))
                        }
                    }
                }
            }
            sw.stop()
            logger.info "Adding KBs with concurrence took ${sw.elapsed(MILLISECONDS)} ms"
        }
    }

    void testListSavedObjectStats() {
        db4o.readOnlyOperation { OC oc ->
            def classCount = new HashMultiset<Class>()

            logger.info 'stored classes:'
            for (sc in oc.ext().storedClasses()) {
                logger.info "${sc.name} - ${sc.instanceCount()} instances"
            }

            logger.info 'known classes:'
            for (kc in oc.ext().knownClasses()) {
                logger.info "${kc.name} - ${kc.array ? 'A' : ''}${kc.collection ? 'C' : ''}"
            }

            for (obj in oc.query(Object.class)) {
                classCount.add(obj.getClass())
            }
            def sorted = classCount.elementSet().sort({ Object o ->
                if (o instanceof Class) return o.name
                o.toString()
            }).reverse()

            for (Class cls in sorted) {
                logger.info sprintf("%6d\t%s%n", classCount.count(cls), cls?.name)
            }
        }
    }

    void testTransparentActivation() {
        ensureSingleSimpleLinkedList()
        db4o.readOnlyOperation { OC oc ->
            def list = oc.query(SimpleLinkedList.class).first()
            assert list.size() == 128
            assert list.get(59) == 60
        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    void testTransparentPersistence() {
        ensureSingleSimpleLinkedList()
        int lastBeforeUpdate
        db4o.writeTransaction { OC oc ->
            SimpleLinkedList<Integer> list = oc.query(new Predicate<SimpleLinkedList<Integer>>() {
                @Override
                boolean match(SimpleLinkedList<Integer> extentType) {
                    return true
                }
            }).first()
            lastBeforeUpdate = list.last
            list.last += 1
        }
        db4o.reopenContainer()
        db4o.readOnlyOperation { OC oc ->
            SimpleLinkedList<Integer> list = oc.query(SimpleLinkedList.class).first()
            assert list.last == lastBeforeUpdate + 1
        }
    }


    void testTAEnhancements() {
        for (Class c in Db4o.Db4oConfig.TRANSPARENT_ACTIVATION_CLASSES) {
            assert Activatable.class.isAssignableFrom(c)
        }
    }


    void ensureSingleSimpleLinkedList() {
        def listCreation = db4o.readOnlyOperation { OC oc ->
            def result = oc.query(SimpleLinkedList.class)
            if (result.size() > 1) {
                logger.warn "More than on SimpleLinkedList in db - this should not have happended..."
            }
            result.size() >= 1 ? false : true
        }
        if (listCreation) {
            def list = new SimpleLinkedList<Integer>(1..128)
            db4o.writeTransaction { OC oc -> oc.store(list) }
            db4o.reopenContainer()
        }
    }

    Db4o getDb4o() {
        if (Environment.byProperty() == Environment.PRODUCTION) {
            throw new IllegalStateException('Bad idea to test with production data')
        }
        Db4o.instance
    }
}
