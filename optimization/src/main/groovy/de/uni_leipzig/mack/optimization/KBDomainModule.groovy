package de.uni_leipzig.mack.optimization

import aksw.org.doodle.similarity.CosineSimilarity
import aksw.org.doodle.similarity.QGramSimilarity
import aksw.org.doodle.similarity.Similarity
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.inject.TypeLiteral
import de.uni_leipzig.mack.evaluation.EvaluationMetric
import de.uni_leipzig.mack.evaluation.KnowledgeBasePool
import de.uni_leipzig.mack.evaluation.MRRanyRelevant
import de.uni_leipzig.mack.evaluation.MRRmostRelevant
import de.uni_leipzig.mack.linkstats.LinkStatistics
import de.uni_leipzig.mack.linkstats.SimCorrellatedLinkStatistics
import de.uni_leipzig.mack.utils.ClassName
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Log4j
import org.opt4j.core.start.Constant
import org.opt4j.core.start.Opt4JModule

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@CompileStatic
@Log4j('logger')
class KBDomainModule extends Opt4JModule {
    KnowledgeBase knowledgeBase = KnowledgeBase.MID_QUARTERS
    Evaluation evalMetrics = Evaluation.MRR_ANY
    @Constant('maxLinks') int maxLinks
    @Constant('linkingThreshold') double linkingThreshold

    @Lazy
    protected KnowledgeBasePool cachedPool = loadPool()

    protected KnowledgeBasePool loadPool() {
        logger.info "Loading pool for '$knowledgeBase.dbLabel"
        KnowledgeBasePool.loadForLabel(knowledgeBase.dbLabel).get()
    }

    @Override
    protected void config() {
        ImmutableList<Similarity> elementarySimilarities =
            ImmutableList.of((Similarity) new QGramSimilarity(), (Similarity) new CosineSimilarity())
        ImmutableMap<ClassName, Double> simDeviationCoefficients =
            ImmutableMap.of(ClassName.forClass(QGramSimilarity.class), 0.5d,
                    ClassName.forClass(CosineSimilarity.class), 0.5d)

        bind(new TypeLiteral<ImmutableList<Similarity>>() {}).toInstance(elementarySimilarities)
        bind(new TypeLiteral<ImmutableMap<ClassName, Double>>() {}).toInstance(simDeviationCoefficients)
        bind(new TypeLiteral<ImmutableList<? extends EvaluationMetric>>() {}).toInstance(evalMetrics.metrics)

        bind(LinkStatistics.class).to(SimCorrellatedLinkStatistics.class).in(SINGLETON)
        bind(CombSimDecoder.SimilarityConverter.class)
                .to(CombSimDecoder.PrecomputedValuesSimilarityConverter.class).in(SINGLETON)
        bind(KnowledgeBasePool.class).toInstance(cachedPool)
    }
}

@CompileStatic
enum KnowledgeBase {
    MID_QUARTERS('midQuarters'), MIN1_MAX100('min1max100')

    KnowledgeBase(String dbLabel) {
        this.dbLabel = dbLabel
    }

    String dbLabel
}

@TypeChecked
enum Evaluation {
    MRR_ANY(ImmutableList.of(new MRRanyRelevant())),
    MRR_BEST(ImmutableList.of(new MRRmostRelevant())),
    MRR_BOTH(ImmutableList.of((EvaluationMetric) new MRRanyRelevant(), (EvaluationMetric) new MRRmostRelevant()))

    ImmutableList<? extends EvaluationMetric> metrics

    Evaluation(ImmutableList<? extends EvaluationMetric> metrics) {
        this.metrics = metrics
    }
}