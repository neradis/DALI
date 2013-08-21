package de.uni_leipzig.mack.optimization.heuristics

import de.uni_leipzig.mack.NoConfMultiOpt
import de.uni_leipzig.mack.optimization.CombSimDecoder
import de.uni_leipzig.mack.optimization.RelevanceEvaluator
import groovy.transform.CompileStatic
import org.opt4j.core.problem.ProblemModule
import org.opt4j.core.start.Constant

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@CompileStatic
class RandomizedStartProblem extends ProblemModule {

    @Constant('dimensions') int dimensions = 2

    @Override
    protected void config() {
        bindProblem(RandomCreator.class, CombSimDecoder.class, RelevanceEvaluator.class)

        addIndividualStateListener(NoConfMultiOpt.Opt4jLoggingListener.class)
        addOptimizerIterationListener(NoConfMultiOpt.Opt4jLoggingListener.class)
    }


}
