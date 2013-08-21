package de.uni_leipzig.mack.processes

import aksw.org.doodle.dataset.Description
import aksw.org.doodle.dataset.VectorDescription
import aksw.org.doodle.engine.Engine
import com.db4o.ObjectContainer
import com.db4o.query.Predicate
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Lists
import de.uni_leipzig.mack.evaluation.KnowledgeBase
import de.uni_leipzig.mack.evaluation.KnowledgeBasePool
import de.uni_leipzig.mack.persistence.Db4o
import groovy.transform.TypeChecked
import groovy.util.logging.Log4j

import static java.util.Map.Entry

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@TypeChecked
@Log4j('logger')
class SaveKnowledgeBases {
    static final Closure<Collection<KnowledgeBase>> MID_QUARTERS = { List<KnowledgeBase> basicList ->
        def quartileLen = (int) basicList.size() / 4
        (Collection<KnowledgeBase>) Lists.newLinkedList(basicList.subList(quartileLen, basicList.size() - quartileLen))
    }

    static final Closure<Collection<KnowledgeBase>> MIN1_MAX100 = { List<KnowledgeBase> basicList ->
        (Collection<KnowledgeBase>) basicList.grep { KnowledgeBase kb ->
            kb.getVectorDescription().features.size() in 1..100
        }
    }

    static final ImmutableMap<String, Closure<Collection<KnowledgeBase>>> NAMED_FILTERS = ImmutableMap.of(
            'midQuarters', MID_QUARTERS,
            'min1max100', MIN1_MAX100
    )

    static void printSortedKBs(Collection<KnowledgeBase> kbs, String title) {

        logger.info "$title (${kbs.size()} KBs)"
        def sorted = kbs.sort({ KnowledgeBase kb -> kb.vectorDescription.features.size() })
        println sorted.collect({ KnowledgeBase kb ->
            def size = kb.vectorDescription.features.size()
            sprintf('%6d\t%.32s', size, kb.label.trim())
        }).join('\n')
    }


    static void main(String[] args) {
        def engineData = Engine.instance.datasets
        def allKBs = engineData.entrySet().collect({ Entry<String, Description> e ->
            e.value instanceof VectorDescription ? new KnowledgeBase(e.key.trim(), (VectorDescription) e.value) : null
        })

        assert allKBs.size() == allKBs.grep().size()

        printSortedKBs(allKBs, 'all datasets')


        for (namedFilter in NAMED_FILTERS.entrySet()) {
            def poolLabel = namedFilter.key
            Closure<Collection<KnowledgeBase>> poolFilter = namedFilter.value

            Collection<KnowledgeBase> subset = poolFilter.call(allKBs)
            printSortedKBs(subset, "adding $poolLabel".toString())

            def freshPool = KnowledgeBasePool.create(poolLabel, subset)
            def sizeBeforePersist = freshPool.size()

            Db4o.instance.writeTransaction { ObjectContainer oc ->
                def result = oc.query(new Predicate<KnowledgeBasePool>() {

                    @Override
                    boolean match(KnowledgeBasePool candidate) {
                        return candidate.getLabel() == poolLabel
                    }
                })

                for (kbp in result) {
                    logger.info "Deleting former pool for '$poolLabel'"
                    oc.delete(kbp)
                }
                oc.commit()
                oc.store(freshPool)
            }

            //Db4o.instance.reopenContainer()

            Db4o.instance.readOnlyOperation {
                ObjectContainer oc ->
                    def result = oc.query(new Predicate<KnowledgeBasePool>() {

                        @Override
                        boolean match(KnowledgeBasePool candidate) {
                            return candidate.getLabel() == poolLabel
                        }
                    })
                    assert result.size() == 1
                    assert result.first().size() == sizeBeforePersist
            }
        }
    }
}
