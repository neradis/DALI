package de.uni_leipzig.mack.evaluation;

import com.google.common.collect.BiMap;

import java.util.Iterator;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public abstract class EvaluationMetric {

    public abstract double compute(Iterator<BiMap<KnowledgeBase,Integer>> rankedQueryResults,
                                   BiMap<KnowledgeBase,Integer> rankedRelevanceReference);
}
