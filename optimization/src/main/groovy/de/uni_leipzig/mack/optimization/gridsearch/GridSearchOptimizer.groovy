package de.uni_leipzig.mack.optimization.gridsearch

import aksw.org.doodle.similarity.Similarity
import com.google.inject.Inject
import groovy.transform.CompileStatic
import org.opt4j.core.IndividualFactory
import org.opt4j.core.optimizer.Control
import org.opt4j.core.optimizer.IterativeOptimizer
import org.opt4j.core.optimizer.Population
import org.opt4j.core.optimizer.TerminationException
import org.opt4j.core.start.Constant

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@CompileStatic
class GridSearchOptimizer<T extends Similarity> implements IterativeOptimizer {


    protected final Population population
    protected final IndividualFactory individualFactory
    protected final Control control
    @Inject
    @Constant('evaluationsPerIteration') int evaluationsPerIteration

    @Inject
    GridSearchOptimizer(Population population, IndividualFactory individualFactory, Control control) {
        this.population = population
        this.individualFactory = individualFactory
        this.control = control
    }

    @Override
    void initialize() throws TerminationException {
    }

    @Override
    void next() throws TerminationException {
        try {
            if (evaluationsPerIteration <= 0) {
                throw new IllegalStateException("need evaluationsPerIteration must be greater than 0")
            }
            evaluationsPerIteration.times {
                population.add(individualFactory.create())
            }
        } catch (GridSearchExhaustedException gse) {
            control.doStop()
        }
    }
}
