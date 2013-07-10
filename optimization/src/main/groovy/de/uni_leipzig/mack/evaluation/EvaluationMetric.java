package de.uni_leipzig.mack.evaluation;

import java.util.Iterator;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public abstract class EvaluationMetric {

    public abstract double compute(Iterator<ResultRelevancePair> resultsWithRelevance);
}
