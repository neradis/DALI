package de.uni_leipzig.mack.evaluation;

import java.util.Iterator;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class MRRmostRelevant extends EvaluationMetric{

    @Override
    public double compute(Iterator<ResultRelevancePair> resultsWithRelevanceIter) {

        int queryCount = 0;
        double mrrNom = 0d;

        while(resultsWithRelevanceIter.hasNext()) {
            queryCount++;
            ResultRelevancePair quRel = resultsWithRelevanceIter.next();

            KnowledgeBase mostRelevant = quRel.getRelevanceEstimation().inverse().get(1);
            Integer rankInResult = quRel.getQueryResult().get(mostRelevant);

            mrrNom += rankInResult == null ? 0d : 1d /rankInResult; //reciprocal rank is 0 if not in query result
        }

        return mrrNom / queryCount; // (sum over (1 / rank_i)) / |Q|
    }
}
