package de.uni_leipzig.mack.persistence

import com.db4o.Db4oEmbedded
import com.db4o.ObjectContainer
import com.db4o.config.EmbeddedConfiguration
import com.db4o.ta.TransparentActivationSupport
import com.db4o.ta.TransparentPersistenceSupport
import com.google.common.collect.ImmutableList
import de.uni_leipzig.mack.config.Config
import de.uni_leipzig.mack.evaluation.KnowledgeBase
import de.uni_leipzig.mack.evaluation.KnowledgeBasePool
import de.uni_leipzig.mack.persistence.models.ArrayTableData
import de.uni_leipzig.mack.persistence.testing.SimpleLinkedList
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Log4j

import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@CompileStatic
@Singleton(lazy = true)
@Log4j('logger')
class Db4o {
    volatile ObjectContainer container = openContainer()
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock()

    def <T> T readOnlyOperation(Closure<T> work) {
        lock.readLock().lock()
        try {
            work.call(container)
        } catch (OutOfMemoryError critEx) {
            logger.error "emergency db4o shutdown due to: ${critEx.class.simpleName}"
            safeRollback()
            closeContainer()
            throw critEx
        } finally {
            lock.readLock().unlock()
        }
    }

    def <T> T writeTransaction(Closure<T> work) {
        lock.writeLock().lock()
        try {
            T result = work.call(container)
            container.commit()
            return result
        } catch (OutOfMemoryError critEx) {
            logger.error "emergency db4o shutdown due to: ${critEx.class.simpleName}"
            safeRollback()
            closeContainer()
            throw critEx
        } catch (Throwable t) {
            logger.warn "db4o rollback in reaction of unhandled throwable: ${t.class.simpleName}"
            safeRollback()
            throw t
        } finally {
            lock.writeLock().unlock()
        }
    }

    void reopenContainer() {
        lock.writeLock().lock()
        try {
            closeContainer()
            assert container.is(null) || container.ext().isClosed()
            this.@container = openContainer()
        } finally {
            lock.writeLock().unlock()
        }
    }

    private ObjectContainer openContainer() {

        Db4oEmbedded.openFile(Db4oConfig.config, getDbFilePath())
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    protected String getDbFilePath() {
        def config = Config.byProperty()
        config.getConf({ it.db4o.file }, String.class)
    }

    protected safeRollback() {
        try {
            container.rollback()
        } catch (DatabaseClosedException) {
            logger.error "unexpected closed state of object container"
        } catch (Throwable t) {
            logger.error "error during rollback:\n$t"
            try {
                container.rollback() //second attempt to prevent unexpected commit in errenous transaction
            } finally {
                logger.error "emergency closing of object container to prevent further inconsistencies"
                closeContainer()
            }
            throw t
        }
    }

    protected closeContainer() {
        try {
            int closeOps = 0
            while (!container.close()) {
                closeOps++
            }
            if (closeOps > 1) {
                logger.warn "There were unexpected open sessions in the shutdown hook ($closeOps close operations)"
            }
        } catch (DatabaseClosedException) {
            logger.error "unexpected closed state of object container"
        } catch (Throwable t) {
            while (!container.close()) {
            }
            throw t
        } finally {
            if (container && !container.ext().isClosed()) {
                logger.error 'Was ultimately unable to close the object container!'
                println '[CRITICAL] Was ultimately unable to close the object container!'
            }
        }
    }

    protected shutdownHook() {
        logger.info 'closing object container in shutdown hook'
        try {
            container.rollback()
            int closeOps = 0
            while (!container.close()) {
                closeOps++
            }
            if (closeOps > 1) {
                logger.warn "There were unexpected open sessions in the shutdown hook ($closeOps close operations)"
            }
        } catch (DatabaseClosedException) {
            logger.info 'object container was already closed (during shutdown hook)'
        } catch (Throwable t) {
            if (container && !container.ext().isClosed()) {
                container.close()
            }
        }
    }

    @TypeChecked
    static class Db4oConfig {
        final static TRANSPARENT_ACTIVATION_CLASSES = ImmutableList.of(ArrayTableData.class, KnowledgeBase.class,
                KnowledgeBasePool.class, SimpleLinkedList.class, SimpleLinkedList.Node.class)
        final static CASCADING_ACTIVATION_CLASSES = ImmutableList.of()
        final static CASCADING_DELETE_CLASSES = ImmutableList.of(ArrayTableData.class, KnowledgeBasePool.class)
        final static CASCADING_UPDATE_CLASSES = ImmutableList.of(ArrayTableData.class)

        static EmbeddedConfiguration getConfig() {
            def conf = Db4oEmbedded.newConfiguration()
            //conf.common().activationDepth(Integer.MAX_VALUE)
            //conf.common().objectClass(SimpleLinkedList.class).cascadeOnActivate(true)
            conf.common().add(new TransparentActivationSupport())
            conf.common().add(new TransparentPersistenceSupport())
            for (c in CASCADING_DELETE_CLASSES) {
                conf.common().objectClass(c).cascadeOnDelete(true)
            }
            for (c in CASCADING_ACTIVATION_CLASSES) {
                conf.common().objectClass(c).cascadeOnActivate(true)
            }
            for (c in CASCADING_UPDATE_CLASSES) {
                conf.common().objectClass(c).cascadeOnUpdate(true)
            }
            return conf
        }
    }
}
