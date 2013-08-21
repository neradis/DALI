package de.uni_leipzig.mack.similarity

import aksw.org.doodle.similarity.Similarity
import com.google.common.base.Objects
import com.google.common.collect.ImmutableMap
import de.uni_leipzig.mack.evaluation.KnowledgeBase
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

import static de.uni_leipzig.mack.utils.Utils.NamedItiem

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@CompileStatic
abstract class WeightedCombinationKBSimilarity implements IKBSimilarity {
    ImmutableMap<IKBSimilarity, Double> similaritiesWithWeights

    WeightedCombinationKBSimilarity(Map<IKBSimilarity, Double> similaritiesWithWeights) {
        this.similaritiesWithWeights = ImmutableMap.copyOf(similaritiesWithWeights)
    }

    WeightedCombinationKBSimilarity(List<IKBSimilarity> similarities, List<Double> weights) {
        this(lists2Map(similarities, weights))
    }

    abstract double getSimilarity(KnowledgeBase kb1, KnowledgeBase kb2)

    ImmutableMap<IKBSimilarity, Double> getSimilaritiesWithWeights() {
        return similaritiesWithWeights
    }

    protected static lists2Map(List<IKBSimilarity> similarities, List<Double> weights) {
        if (similarities.size() != weights.size()) {
            throw new IllegalArgumentException("Lists for similiarities and weights must be of same size")
        }

        def weigthsIter = weights.iterator()
        def map = new LinkedHashMap<IKBSimilarity, Double>(similarities.size())
        for (sim in similarities) {
            map.put(sim, weigthsIter.next())
        }
        return map
    }

    protected Map<String, String> readableSimNamesAndWeights() {
        def withNamesOrdered = similaritiesWithWeights.keySet().collect({ IKBSimilarity sim ->
            def anonSimCounter = 1
            if (sim instanceof KBSimilarity) {
                return new NamedItiem<IKBSimilarity>(sim, sim.baseSimilarity.class.simpleName)
            } else {
                return new NamedItiem<IKBSimilarity>(sim, "Sim${anonSimCounter++}".toString())
            }
        }).sort({ NamedItiem ni -> ni.name })

        def shortWeight = { IKBSimilarity kbsim ->
            sprintf('%.2f', similaritiesWithWeights.get(kbsim))
        }

        def result = new LinkedHashMap<String, String>()
        for (namedSim in withNamesOrdered) {
            result.put(namedSim.getName(), shortWeight(namedSim.getItem()))
        }
        return result
    }

    @Override
    String toString() {
        def namesWithWeights = readableSimNamesAndWeights()

        Objects.toStringHelper(this).add('similarities', namesWithWeights.keySet())
                .add('weights', namesWithWeights.values())
    }
}

@CompileStatic
class LinearCombinationKBSimilarity extends WeightedCombinationKBSimilarity {

    LinearCombinationKBSimilarity(Map<IKBSimilarity, Double> similaritiesWithWeights) {
        super(similaritiesWithWeights)
    }

    LinearCombinationKBSimilarity(List<IKBSimilarity> similarities, List<Double> weights) {
        super(similarities, weights)
    }

    @Override
    @CompileStatic(TypeCheckingMode.SKIP)
    double getSimilarity(KnowledgeBase kb1, KnowledgeBase kb2) {
        similaritiesWithWeights.entrySet().inject(0d) { double sum, Map.Entry<Similarity, Double> e ->
            sum += e.value * e.key.getSimilarity(kb1, kb2); sum
        }
    }

    @Override
    String toString() {
        def formular = readableSimNamesAndWeights().entrySet().collect({ Map.Entry<String, String> e ->
            e.value + '*' + e.key
        }).join('+')

        "${this.getClass().simpleName}[$formular]".toString()
    }
}
