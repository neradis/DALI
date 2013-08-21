package de.uni_leipzig.mack.evaluation

import com.google.common.collect.ImmutableSet
import groovy.transform.CompileStatic

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@CompileStatic
abstract class PooledRelevanceEstimator {

    KnowledgeBasePool pool

    PooledRelevanceEstimator(KnowledgeBasePool pool) {
        this.pool = ImmutableSet.copyOf(pool)
    }

    /**
     * decides if a candidate knowledge base is relevant with respect to a given knowledge base
     *
     * Note: return values must the consistent with relevanteComparision(), e.g.
     * if if isRelevant(q,a) returns true and isRelevant(q,b) returns false, then relevanceComparison(q,a,b)
     * must return a positive integer
     * if both is Relevant(q,a) and isRelevant(q,b) return false, then relevanceComparison(q,a,b) must return 0
     * if both is Relevant(q,a) and isRelevant(q,b) return true, relevanceComparison(q,a,b) can return any value
     * depending upon a more fine-grained estimation of relevance
     *
     * @param query the knowledge base defining the reference point to judge relevance on
     * @param candidate the knowledge base for which relevance has to be decided (is candidate relevant for query?)
     * @return true , if candidate is relevant for query, false otherwise
     */
    abstract boolean isRelevant(KnowledgeBase query, KnowledgeBase candidate);

    /**
     * compare relevance of two candidate knowledge bases (results of a retrieval function) with respect to a given
     * knowledge base (the argument for the retrieval function searching for similar knowledge bases)
     *
     * @param query the knowledge base for which relevance is to be judges for (e.g. is candidateA more relevant
     *                     as result for query as candidateB?)
     * @param candidateA first candidate for relevance
     * @param candidateB second candidate for relevance
     * @return a negative integer, zero, or a positive integer as candidateA is less, equally to, or more relevant
     *         to query that candidateB
     */
    abstract int relevanceComparison(KnowledgeBase query, KnowledgeBase candidateA, KnowledgeBase candidateB);

    /**
     * find the Knowledge base deemed most relevant for {@code query} in the pool
     * of considered knowledge bases
     *
     * @param query knowledge base defining the query (by example)
     * @return most relevant knowledge base
     */
    KnowledgeBase getMostRelevantBase(KnowledgeBase query) {
        pool.max({ KnowledgeBase a, KnowledgeBase b ->
            relevanceComparison(query, a, b) } as Comparator<KnowledgeBase>)
    }

    /**
     * fetch subset of knowledge bases deemed relevant for {@code query} in the pool
     * of considered knowledge bases
     *
     * @param query knowledge base defining the query (by example)
     * @return most relevant knowledge base
     */
    Collection<KnowledgeBase> getRelevantBases(KnowledgeBase query) {
        pool.grep { KnowledgeBase cand -> isRelevant(query, cand) }
    }

    KnowledgeBasePool getPool() {
        return pool
    }
}
