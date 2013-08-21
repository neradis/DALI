package de.uni_leipzig.mack.optimization

import com.google.common.base.Stopwatch
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.ImmutableList
import com.google.common.collect.Maps
import com.google.inject.Inject
import de.uni_leipzig.mack.evaluation.EvaluationMetric
import de.uni_leipzig.mack.evaluation.KnowledgeBase
import de.uni_leipzig.mack.evaluation.KnowledgeBasePool
import de.uni_leipzig.mack.evaluation.ResultRelevancePair
import de.uni_leipzig.mack.linkstats.LinkStatistics
import de.uni_leipzig.mack.similarity.IKBSimilarity
import groovy.transform.TypeChecked
import groovy.util.logging.Log4j
import org.opt4j.core.Objectives
import org.opt4j.core.problem.Evaluator

import java.util.Map.Entry
import java.util.concurrent.ConcurrentMap

import static java.util.concurrent.TimeUnit.MILLISECONDS
import static org.opt4j.core.Objective.Sign

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@TypeChecked
@Log4j('logger')
class RelevanceEvaluator implements Evaluator<IKBSimilarity> {
    private static final ConcurrentMap<String, Long> maxEvalTimes = Maps.newConcurrentMap()

    protected final ImmutableList<EvaluationMetric> evalMetrics
    protected final KnowledgeBasePool pool
    protected final LinkStatistics linkStats

    @Inject
    RelevanceEvaluator(ImmutableList<? extends EvaluationMetric> evalMetrics,
                       KnowledgeBasePool pool, LinkStatistics linkStats) {
        this.evalMetrics = evalMetrics
        this.pool = pool
        this.linkStats = linkStats
    }

    @Override
    Objectives evaluate(IKBSimilarity similarity) {
        def objectives = new Objectives()
        for (metric in evalMetrics) {
            def sw = new Stopwatch().start()
            def evalMeasure = metric.compute(simRankIter(similarity))
            sw.stop()
            if (logger.traceEnabled && maxEvalTimes.updateMaximum('evaluation', sw.elapsed(MILLISECONDS))) {
                logger.trace "new peak for 'evaluation' for '$metric.name': ${sw.elapsed(MILLISECONDS)} ms"
            }
            objectives.add(metric.name, Sign.MAX, evalMeasure)
        }
        return objectives
    }

    ImmutableList<EvaluationMetric> getEvalMetrics() {
        return evalMetrics
    }

    protected Iterator<ResultRelevancePair> simRankIter(IKBSimilarity similarity) {
        pool.iterator().transform { KnowledgeBase kb ->
            new ResultRelevancePair(simRanking(kb, similarity), linkRanking(kb))
        }
    }

    protected BiMap<KnowledgeBase, Integer> simRanking(KnowledgeBase kb, IKBSimilarity similarity) {

        def eval = { KnowledgeBase ref, KnowledgeBase other ->
            similarity.getSimilarity(ref, other)
        }

        def sw = new Stopwatch().start()
        def ret = computeRankings(kb, eval, { double sim -> sim > 0 })
        sw.stop()
        if (logger.traceEnabled && maxEvalTimes.updateMaximum('simRanking', sw.elapsed(MILLISECONDS))) {
            logger.trace "new peak for 'simRanking' for '$kb.label': ${sw.elapsed(MILLISECONDS)} ms"
        }
        ret
    }

    protected BiMap<KnowledgeBase, Integer> linkRanking(KnowledgeBase kb) {

        def eval = { KnowledgeBase ref, KnowledgeBase other ->
            linkStats.getLinkCount(ref, other)
        }

        def sw = new Stopwatch().start()
        def ret = computeRankings(kb, eval, { int links -> links > 0 })
        sw.stop()
        if (logger.traceEnabled && maxEvalTimes.updateMaximum('linkRanking', sw.elapsed(MILLISECONDS))) {
            logger.trace "new peak for 'linkRanking' for '$kb.label': ${sw.elapsed(MILLISECONDS)} ms"
        }
        ret
    }

    protected <V extends Comparable<V>> BiMap<KnowledgeBase, Integer> computeRankings(KnowledgeBase kb,
                                                                                      Closure<V> measure, Closure<Boolean> filter) {
        assert kb in pool
        Map<KnowledgeBase, V> valMap = Maps.newHashMap()
        def sw = new Stopwatch()

        for (other in pool) {
            if (kb != other) {
                sw.reset().start()
                V val = (V) measure.call(kb, other) //TODO: check if cast is really needed
                sw.stop()
                if (logger.traceEnabled &&
                        maxEvalTimes.updateMaximum(measure.class.simpleName, sw.elapsed(MILLISECONDS))) {
                    logger.trace "new peak for '${measure.class.simpleName}' for ($kb.label,$other.label): " +
                            "${sw.elapsed(MILLISECONDS)} ms"
                }
                if (filter(val)) valMap.put(other, val)
            }
        }
        map2rankedBimap(valMap)
    }

    protected static <V extends Comparable<V>> BiMap<KnowledgeBase, Integer> map2rankedBimap(Map<KnowledgeBase, V> stats) {
        def grouped = stats.groupBy { Entry<KnowledgeBase, V> e -> e.value }
        def sortedValues = grouped.keySet().sort { V v1, V v2 -> -v1.compareTo(v2) } //sort desc.
        BiMap<KnowledgeBase, Integer> ranks = HashBiMap.create(stats.size())
        int rank = 1
        for (val in sortedValues) {
            for (kb in grouped.get(val).keySet().sort { KnowledgeBase k -> k.label }) {
                ranks.put(kb, rank++)
            }
        }
        return ranks
    }
}
