package de.uni_leipzig.mack.evaluation;

import java.util.Iterator;

/**
 * 
 * @author Mirko Pinseler
 * 
 */
public class MAP extends EvaluationMetric {

    @Override
    public double compute(Iterator<ResultRelevancePair> resultsWithRelevanceIter) {
        int queryCount = 0;
        double sum = 0d;
        while (resultsWithRelevanceIter.hasNext()) {
            queryCount++;
            ResultRelevancePair quRel = resultsWithRelevanceIter.next();
            sum += avgP(quRel);
        }
        return sum / queryCount;
    }

    private double avgP(ResultRelevancePair quRel) {
        double sum = 0d;
        for (int count = 1; count <= quRel.getQueryResult().size(); count++) {
            if (quRel.getRelevanceEstimation.contains(quRel.getQueryResult()
                    .inverse().get(count))) {
                sum += patn(quRel, count);
            }
        }
        return sum / quRel.getQueryResult().size();
    }

    private double patn(ResultRelevancePair quRel, int n){
        double sum = 0d;
        if(quRel.getQueryResult().size() >= n){
            for(int count = 1; count <= n; count++){
                if(quRel.getRelevanceEstimation.contains(quRel.getQueryResult().inverse().get(count))){
                    double sum += 1;
                }
            }
        }
        return sum/n;
    }
}
