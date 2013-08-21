package de.uni_leipzig.mack.similarity

import aksw.org.doodle.dataset.Description
import aksw.org.doodle.similarity.Similarity
import de.uni_leipzig.mack.evaluation.KnowledgeBase
import de.uni_leipzig.mack.evaluation.KnowledgeBasePool
import de.uni_leipzig.mack.persistence.LoadError
import de.uni_leipzig.mack.persistence.SimilarityValuesTable
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

/**
 * A wrapper around {@link Similarity} that uses {@link KnowledgeBase} parameters instead of {@link Description}
 *
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@CompileStatic
interface IKBSimilarity {
    double getSimilarity(KnowledgeBase kb1, KnowledgeBase kb2)
}

@CompileStatic
class KBSimilarity implements IKBSimilarity {

    protected Similarity baseSimilarity

    KBSimilarity(Similarity baseSimilarity) {
        this.baseSimilarity = baseSimilarity
    }

    Similarity getBaseSimilarity() {
        return baseSimilarity
    }

    @Override
    double getSimilarity(KnowledgeBase kb1, KnowledgeBase kb2) {
        if ([kb1, kb2].any { KnowledgeBase k -> k.vectorDescription.is(null) }) {
            throw new IllegalArgumentException("Provided KnowledgeBases cannot have null-Vectordescriptions")
        }
        baseSimilarity.getSimilarity(kb1.vectorDescription, kb2.vectorDescription)
    }
}

@CompileStatic
@Log4j('logger')
class PrecomputedKBSimilarity extends KBSimilarity {
    protected SimilarityValuesTable memory

    private PrecomputedKBSimilarity(Similarity baseSimilarity) {
        super(baseSimilarity)
    }

    protected PrecomputedKBSimilarity(Similarity baseSimilarity, SimilarityValuesTable svt) {
        super(baseSimilarity)
        this.memory = svt
    }

    static PrecomputedKBSimilarity load(Similarity baseSimilarity, KnowledgeBasePool kbp)
    throws LoadError {
        def memory = SimilarityValuesTable.forSimilarityAndKnowledgeBase(baseSimilarity.getClass(), kbp)
        new PrecomputedKBSimilarity(baseSimilarity, memory)
    }

    @Override
    double getSimilarity(KnowledgeBase kb1, KnowledgeBase kb2) {
        def result = memory.getSimilarity(kb1, kb2)
        if (result.is(null)) {
            logger.warn "similarity for ${kb1.getLabel()} and ${kb2.getLabel()} not found in memory"
            result = super.getSimilarity(kb1, kb2)
            if (kb1 in memory.rowKeySet() && kb2 in memory.columnKeySet()) {
                logger.info "could add similarity $result for for ${kb1.getLabel()} and ${kb2.getLabel()} " +
                        "(similarity: ${baseSimilarity.class.simpleName}," +
                        " kb: ${memory.knowledgeBasePool.label})"
            }
        }
        return result
    }
}
