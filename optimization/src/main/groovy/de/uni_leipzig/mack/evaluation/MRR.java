package de.uni_leipzig.mack.evaluation;

import com.google.common.collect.BiMap;
import com.google.common.collect.Sets;

import java.util.Iterator;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class MRR extends EvaluationMetric {
    @Override
    public double compute(Iterator<BiMap<KnowledgeBase, Integer>> rankedQueryResults,
                          BiMap<KnowledgeBase, Integer> rankedRelevanceReference) {

        int queryCount = 0;
        double mrrNom = 0d;

        while(rankedQueryResults.hasNext()) {
            queryCount++;
            BiMap<KnowledgeBase, Integer> queryResult = rankedQueryResults.next();
            int minimalRank = Integer.MAX_VALUE;
            for(KnowledgeBase relevantResult : Sets.intersection(queryResult.keySet(),rankedRelevanceReference.keySet())) {
                int resultRank = queryResult.get(relevantResult);
                minimalRank = resultRank < minimalRank ? resultRank : minimalRank;
                mrrNom += 1d / minimalRank;
            }
        }

        return mrrNom / queryCount;
    }
}
