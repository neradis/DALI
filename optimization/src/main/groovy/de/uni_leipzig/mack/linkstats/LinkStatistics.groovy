package de.uni_leipzig.mack.linkstats

import aksw.org.doodle.similarity.Similarity
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.inject.Inject
import de.uni_leipzig.mack.evaluation.KnowledgeBase
import de.uni_leipzig.mack.utils.ClassName
import groovy.transform.CompileStatic
import org.opt4j.core.start.Constant

import static de.uni_leipzig.mack.optimization.CombSimDecoder.SimilarityConverter
import static de.uni_leipzig.mack.utils.Utils.arithmeticMean

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@CompileStatic
interface LinkStatistics {

    int getLinkCount(KnowledgeBase origin, KnowledgeBase target)
}

@CompileStatic
class RandomizedLinkStatistics implements LinkStatistics {
    protected static final Random rand = new Random()
    public static final int MAX_LINK_COUNT = 1000

    @Override
    int getLinkCount(KnowledgeBase origin, KnowledgeBase target) {
        rand.nextInt(MAX_LINK_COUNT + 1)
    }
}

@CompileStatic
class SimCorrellatedLinkStatistics implements LinkStatistics {
    public static final int RANDOM_SEED = 1857
    protected static final Random rand = new Random(RANDOM_SEED)

    final int maxLinks
    final double linkingThreshhold
    final ImmutableMap<ClassName, Double> deviationsCoefficientsBySimilarity
    final SimilarityConverter similarityConverter
    final ImmutableList<Similarity> baseSimilarities

    @Inject
    SimCorrellatedLinkStatistics(SimilarityConverter similarityConverter,
                                 ImmutableList<Similarity> baseSimilarities,
                                 ImmutableMap<ClassName, Double> deviationsCoefficientsBySimilarity,
                                 @Constant('maxLinks') int maxLinks,
                                 @Constant('linkingThreshold') double linkingThreshold) {
        this.similarityConverter = similarityConverter
        this.baseSimilarities = baseSimilarities
        this.deviationsCoefficientsBySimilarity = deviationsCoefficientsBySimilarity
        this.maxLinks = maxLinks
        this.linkingThreshhold = linkingThreshold
    }

    @Override
    int getLinkCount(KnowledgeBase origin, KnowledgeBase target) {
        def dSims = baseSimilarities.collect({ Similarity sim -> deviatedSimilarity(sim, origin, target) })
        def dSimCombined = arithmeticMean(dSims)
        dSimCombined > linkingThreshhold ? Math.round(dSimCombined * maxLinks) : 0
    }

    double deviatedSimilarity(Similarity sim, KnowledgeBase origin, KnowledgeBase target) {
        def exactSim = similarityConverter.convert(sim).getSimilarity(origin, target)
        def devCoeff = deviationsCoefficientsBySimilarity.get(ClassName.forClass(sim.getClass()))
        def devFactor = 1 + ((devCoeff != 0) ? (rand.nextBoolean() ? 1d : -1d) * devCoeff * rand.nextDouble() : 0d)
        def deviatedSim = exactSim * devFactor
        return deviatedSim.bounded(0d, 1d)
    }

    ImmutableMap<ClassName, Double> getDeviationsCoefficientsBySimilarity() {
        return deviationsCoefficientsBySimilarity
    }
}
