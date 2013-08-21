package de.uni_leipzig.mack.evaluation;

import com.google.common.collect.Sets;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class MRRanyRelevant extends EvaluationMetric {

    @Override
    public String getName() {
        return "MRR-any";
    }

    @Override
    public double compute(Iterator<ResultRelevancePair> resultsWithRelevanceIter) {

        int queryCount = 0;
        double mrrNom = 0d;

        while (resultsWithRelevanceIter.hasNext()) {
            queryCount++;
            ResultRelevancePair quRel = resultsWithRelevanceIter.next();
            int minimalRank = Integer.MAX_VALUE;
            Set<KnowledgeBase> intersection = Sets.intersection(quRel.getQueryResult().keySet(),
                    quRel.getRelevanceEstimation().keySet());

            if (!intersection.isEmpty()) {
                for (KnowledgeBase relevantResult : intersection) { //find the relevant query result with lowest rank
                    int resultRank = quRel.getQueryResult().get(relevantResult);
                    minimalRank = resultRank < minimalRank ? resultRank : minimalRank;
                }
                mrrNom += 1d / minimalRank; // next summand for 1 / rank_i
            } //if there is no relevant query result reciprocal rank is null -> no update for mrrNom needed
        }
        return mrrNom / queryCount; // (sum over 1 / rank_i) / |Q|
    }
}
