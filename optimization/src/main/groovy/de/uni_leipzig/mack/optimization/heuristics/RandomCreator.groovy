package de.uni_leipzig.mack.optimization.heuristics

import com.google.inject.Inject
import groovy.transform.CompileStatic
import org.opt4j.core.common.random.Rand
import org.opt4j.core.genotype.DoubleGenotype
import org.opt4j.core.problem.Creator
import org.opt4j.core.start.Constant

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@CompileStatic
class RandomCreator implements Creator<DoubleGenotype> {
    protected int dimensions
    protected Rand random

    @Inject
    RandomCreator(@Constant('dimensions') int dimensions, Rand random) {
        this.dimensions = dimensions
        this.random = random
    }

    @Override
    DoubleGenotype create() {
        def result = new DoubleGenotype()
        result.init(random, dimensions)
        return result
    }
}
