package de.uni_leipzig.mack.evaluation;

import com.google.common.collect.BiMap;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class ResultRelevancePair {

    private BiMap<KnowledgeBase,Integer>  queryResult;
    private BiMap<KnowledgeBase,Integer>  relevanceEstimation;

    public ResultRelevancePair(BiMap<KnowledgeBase, Integer> queryResult,
                               BiMap<KnowledgeBase, Integer> relevanceEstimation) {
        this.queryResult = queryResult;
        this.relevanceEstimation = relevanceEstimation;
    }

    public BiMap<KnowledgeBase, Integer> getQueryResult() {
        return queryResult;
    }

    public BiMap<KnowledgeBase, Integer> getRelevanceEstimation() {
        return relevanceEstimation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResultRelevancePair that = (ResultRelevancePair) o;

        if (queryResult != null ? !queryResult.equals(that.queryResult) : that.queryResult != null) return false;
        if (relevanceEstimation != null ? !relevanceEstimation.equals(that.relevanceEstimation)
                                        : that.relevanceEstimation != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = queryResult != null ? queryResult.hashCode() : 0;
        result = 31 * result + (relevanceEstimation != null ? relevanceEstimation.hashCode() : 0);
        return result;
    }
}
