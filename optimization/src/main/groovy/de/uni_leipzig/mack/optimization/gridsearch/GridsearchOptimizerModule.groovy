package de.uni_leipzig.mack.optimization.gridsearch

import groovy.transform.CompileStatic
import org.opt4j.core.optimizer.MaxIterations
import org.opt4j.core.optimizer.OptimizerModule
import org.opt4j.core.start.Constant

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@CompileStatic
class GridsearchOptimizerModule extends OptimizerModule {

    double step = 0.1
    @Constant('evaluationsPerIteration') int evaluationsPerIteration = 10

    @MaxIterations
    int maxIterations = 1000

    @Override
    protected void config() {
        bindConstant('step').to(step)

        bindIterativeOptimizer(GridSearchOptimizer.class)
    }
}
