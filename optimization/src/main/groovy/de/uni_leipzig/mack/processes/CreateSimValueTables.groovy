package de.uni_leipzig.mack.processes

import aksw.org.doodle.dataset.VectorDescription
import aksw.org.doodle.similarity.QGramSimilarity
import com.db4o.ObjectContainer
import com.db4o.ObjectSet
import de.uni_leipzig.mack.evaluation.KnowledgeBasePool
import de.uni_leipzig.mack.persistence.Db4o
import de.uni_leipzig.mack.persistence.SimilarityValuesTable
import groovy.transform.TypeChecked
import groovy.util.logging.Log4j

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@TypeChecked
@Log4j('logger')
class CreateSimValueTables {

    static void diffMaps(VectorDescription orig, VectorDescription pers) {
        assert orig.getName() == pers.getName()
        assert orig.features.keySet() == pers.features.keySet()
        def diff = orig.features.entrySet().grep { Map.Entry<String, Double> e -> e.value != pers.features.get(e.key) }
        if (!diff.empty) {
            logger.info "Differences for '${orig.getName()}' (orig/pers):"
            logger.info diff.collect({ Map.Entry<String, Double> e ->
                sprintf('%-16.16s\t%8.1f\t%8.1f', e.key, e.value, pers.features.get(e.key)) }).join('\n')

        }
    }

    /*def key = kbPool.vectorDescription.label.grep({ it.contains('lobid')}).first()
    def features = kbPool.vectorDescription.grep({it.label == key}).first()
    features.article*/

    static void printlobidArticle(KnowledgeBasePool kbPool, String prefix = '') {
        def key = kbPool*.getLabel().grep({ String s -> s.contains('lobid') }).first()
        def features = kbPool*.getVectorDescription().grep({ VectorDescription v -> v.getName() == key }).first().features
        logger.info "$prefix article -> ${features.get('article')}"
    }


    static void main(String[] args) {
        logger.info 'execution start'
        for (name in SaveKnowledgeBases.NAMED_FILTERS.keySet()) {
            logger.info "fetching KB for $name"

            KnowledgeBasePool pool = Db4o.instance.readOnlyOperation { ObjectContainer oc ->
                ObjectSet<KnowledgeBasePool> results = oc.queryByExample(new KnowledgeBasePool(name, null))
                assert results.size() == 1
                def ret = results.first()
                //oc.activate(ret, Integer.MAX_VALUE)
                return ret
            }

            logger.info "retrieval/compuation start for $name"
            SimilarityValuesTable.forSimilarityAndKnowledgeBase(QGramSimilarity.class, pool)
        }
    }
}