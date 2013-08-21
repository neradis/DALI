package de.uni_leipzig.mack.optimization

import aksw.org.doodle.similarity.Similarity
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.common.collect.ImmutableList
import com.google.inject.Inject
import de.uni_leipzig.mack.evaluation.KnowledgeBasePool
import de.uni_leipzig.mack.similarity.IKBSimilarity
import de.uni_leipzig.mack.similarity.KBSimilarity
import de.uni_leipzig.mack.similarity.LinearCombinationKBSimilarity
import de.uni_leipzig.mack.similarity.PrecomputedKBSimilarity
import groovy.transform.TypeChecked
import org.opt4j.core.genotype.DoubleGenotype
import org.opt4j.core.problem.Decoder

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@TypeChecked
class CombSimDecoder implements Decoder<DoubleGenotype, IKBSimilarity> {
    ImmutableList<Similarity> elementarySimilarities
    protected SimilarityConverter similarityConverter

    @Inject
    CombSimDecoder(ImmutableList<Similarity> elementarySimilarities, SimilarityConverter converter) {
        this.elementarySimilarities = elementarySimilarities
        this.similarityConverter = converter
    }

    @Override
    IKBSimilarity decode(DoubleGenotype genotype) {
        if (genotype.size() != elementarySimilarities.size()) {
            def msg = "Sizes of genotype (${genotype.size()} values) and similiarities " +
                    "(${elementarySimilarities.size()} metrics) do not match!"
            throw new IllegalArgumentException(msg)
        }
        def kbSimilarities = elementarySimilarities.collect(similarityConverter.&convert)

        new LinearCombinationKBSimilarity(kbSimilarities, genotype)
    }

    ImmutableList<Similarity> getElementarySimilarities() {
        return elementarySimilarities
    }

    static interface SimilarityConverter {

        IKBSimilarity convert(Similarity similarity)
    }

    static class SimpleWrappingSimilarityConverter implements SimilarityConverter {

        @Override
        IKBSimilarity convert(Similarity similarity) {
            new KBSimilarity(similarity)
        }
    }

    static class PrecomputedValuesSimilarityConverter implements SimilarityConverter {
        protected KnowledgeBasePool pool
        protected LoadingCache<Similarity, PrecomputedKBSimilarity> cache = CacheBuilder.newBuilder().build(
                { Similarity s -> PrecomputedKBSimilarity.load(s, pool) } as CacheLoader)

        @Inject
        PrecomputedValuesSimilarityConverter(KnowledgeBasePool pool) {
            this.pool = pool
        }

        @Override
        IKBSimilarity convert(Similarity similarity) {
            cache.get(similarity)
        }
    }
}
