package de.uni_leipzig.mack.persistence

import aksw.org.doodle.similarity.Similarity
import com.db4o.ObjectContainer
import com.db4o.query.Predicate
import com.google.common.base.Optional
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import de.uni_leipzig.mack.evaluation.KnowledgeBase
import de.uni_leipzig.mack.evaluation.KnowledgeBasePool
import de.uni_leipzig.mack.persistence.models.ArrayTableData
import de.uni_leipzig.mack.persistence.util.PersistableArrayTable
import de.uni_leipzig.mack.similarity.KBSimilarity
import de.uni_leipzig.mack.utils.Combinatorics
import groovy.transform.TypeChecked
import groovy.util.logging.Log4j
import groovyx.gpars.GParsPool

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantReadWriteLock

import static de.uni_leipzig.mack.utils.Combinatorics.CombinationPair

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@TypeChecked
@Log4j('logger')
class SimilarityValuesTable implements Table<KnowledgeBase, KnowledgeBase, Double> {
    protected static final Table<String, KnowledgeBasePool, SimilarityValuesTable> singletonTable = HashBasedTable.create()
    protected static final singletonTableLock = new Object()
    protected @Delegate PersistableArrayTable<KnowledgeBase, KnowledgeBase, Double> delegate
    protected String baseSimilarityClassName
    protected KnowledgeBasePool knowledgeBasePool
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock()

    static SimilarityValuesTable forSimilarityAndKnowledgeBase(Class simClass,
                                                               KnowledgeBasePool kbp) {
        final className = simClass.getName()
        final tableUUID = "sim_${className}_${kbp.getLabel()}".toString()
        synchronized (singletonTableLock) {
            if (!singletonTable.contains(className, kbp)) {
                def loadedTable = loadPersistableTableFor(tableUUID)
                if (loadedTable.present) {
                    logger.debug "loaded table from db for ${tableUUID}"
                    def simTable = new SimilarityValuesTable(className, kbp, loadedTable.get())
                    singletonTable.put(className, kbp, simTable)
                } else {
                    logger.debug "computing new table for ${tableUUID}"
                    def simTable = computeAndPersist(tableUUID, simClass, kbp)
                    singletonTable.put(className, kbp, simTable)
                }
            }
        }
        singletonTable.get(className, kbp)
    }

    protected SimilarityValuesTable(String baseSimilarityClassName, KnowledgeBasePool knowledgeBasePool,
                                    PersistableArrayTable<KnowledgeBase, KnowledgeBase, Double> delegate) {
        this.baseSimilarityClassName = baseSimilarityClassName
        this.knowledgeBasePool = knowledgeBasePool
        this.delegate = delegate
    }

    protected static SimilarityValuesTable computeAndPersist(String uuid, Class simClass, KnowledgeBasePool kbp) {
        assert Similarity.class.isAssignableFrom(simClass)
        final className = simClass.simpleName
        final Similarity sim = (Similarity) simClass.getConstructor().newInstance()
        final KBSimilarity kbsim = new KBSimilarity(sim)
        final AtomicInteger computed = new AtomicInteger(0)
        final sortedNames = kbp.sort()
        final table = PersistableArrayTable.create(uuid, sortedNames, sortedNames)
        final simTable = new SimilarityValuesTable(simClass.getName(), kbp, table)

        def pairs = Combinatorics.symmetricInnerCombinations(sortedNames)
        GParsPool.withPool() {
            pairs.eachParallel { Combinatorics.CombinationPair<KnowledgeBase> pair ->
                if (!simTable.containsSimilarity(pair.first, pair.second)) {
                    def simVal = kbsim.getSimilarity(pair.first, pair.second)
                    simTable.putSimilarity(pair.first, pair.second, simVal)
                    if (logger.traceEnabled && computed.getAndIncrement() % 100 == 0) {
                        logger.trace "${computed.get()} sim computations done (similiarity: $className, " +
                                "knowledge base pool: ${kbp.getLabel()})"
                    }
                }
            }
        }
        assert simTable.similaritiesComplete()
        table.save(Double.class)
        return simTable
    }

    protected static Optional<PersistableArrayTable> loadPersistableTableFor(String uuid) {
        Db4o.instance.readOnlyOperation { ObjectContainer oc ->
            def results = oc.query(new Predicate<ArrayTableData>() {

                @Override
                boolean match(ArrayTableData candidate) {
                    candidate.getUuid() == uuid
                }
            })
            switch (results.size()) {
                case 0: return Optional.absent()
                case 1: return Optional.of(PersistableArrayTable.fromData(uuid, results.first()))
                case 2: throw new LoadError("More than one PAT for uuid '$uuid' in database")
            }
        }
    }

    boolean containsSimilarity(KnowledgeBase kb1, KnowledgeBase kb2) {
        def sorted = [kb1, kb2].sort() //similarity is a symmetric relationship - query for a kb pair should be
        lock.readLock().lock()
        try {
            !delegate.get(sorted.first(), sorted.second()).is(null)
        } finally {
            lock.readLock().unlock()
        }
    }

    Double getSimilarity(KnowledgeBase kb1, KnowledgeBase kb2) {
        def sorted = [kb1, kb2].sort() //similarity is a symmetric relationship - query for a kb pair should be
        lock.readLock().lock()
        try {
            (Double) delegate.get(sorted.first(), sorted.second()) //normalized accordingly
        } finally {
            lock.readLock().unlock()
        }
    }

    Double putSimilarity(KnowledgeBase kb1, KnowledgeBase kb2, double sim) {
        def sorted = [kb1, kb2].sort() //similarity is a symmetric relationship - storage kb pair should be
        lock.writeLock().lock()
        try {
            (Double) delegate.put(sorted.first(), sorted.second(), sim)
        } finally {
            lock.writeLock().unlock()
        }
    }

    boolean similaritiesComplete() {
        lock.readLock().lock()
        try {
            Combinatorics.symmetricInnerCombinations(rowKeySet().sort()).each { CombinationPair<KnowledgeBase> p ->
                contains(p.first, p.second)
            }
        } finally {
            lock.readLock().unlock()
        }
    }


    String getBaseSimilarityClassName() {
        return baseSimilarityClassName
    }

    KnowledgeBasePool getKnowledgeBasePool() {
        return knowledgeBasePool
    }
}
