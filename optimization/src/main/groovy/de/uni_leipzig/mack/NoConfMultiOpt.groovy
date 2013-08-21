package de.uni_leipzig.mack

import com.google.common.base.Stopwatch
import de.uni_leipzig.mack.optimization.Evaluation
import de.uni_leipzig.mack.optimization.KBDomainModule
import de.uni_leipzig.mack.optimization.KnowledgeBase
import de.uni_leipzig.mack.optimization.gridsearch.GridSearchProblemModule
import de.uni_leipzig.mack.optimization.gridsearch.GridsearchOptimizerModule
import groovy.transform.TypeChecked
import groovy.util.logging.Log4j
import org.opt4j.core.Individual
import org.opt4j.core.IndividualStateListener
import org.opt4j.core.optimizer.Archive
import org.opt4j.core.optimizer.OptimizerIterationListener
import org.opt4j.core.start.Opt4JTask
import org.opt4j.viewer.ViewerModule

import static java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
@TypeChecked
@Log4j('logger')
public class NoConfMultiOpt {    // ca. 38 sec; deviations: QGram 0.5, Cosine 0.5

    public static void main(String[] args) {
        def domain = new KBDomainModule()
        domain.evalMetrics = Evaluation.MRR_BOTH
        domain.knowledgeBase = KnowledgeBase.MID_QUARTERS
        domain.maxLinks = 10000
        domain.linkingThreshold = 0.1

        def problem = new GridSearchProblemModule()
        problem.dimensions = 2

        def optimizer = new GridsearchOptimizerModule()
        optimizer.step = 0.2
        optimizer.evaluationsPerIteration = 4
        optimizer.maxIterations = 25

        ViewerModule viewer = new ViewerModule()
        viewer.setCloseOnStop(false)

        Opt4JTask task = new Opt4JTask(false)

        Stopwatch sw = new Stopwatch().start()
        task.init(domain, problem, optimizer, viewer)
        /*problem.addOptimizerIterationListener(Opt4jLoggingListener.class)
        problem.addIndividualStateListener(Opt4jLoggingListener.class)*/


        sw.stop()
        logger.info "Initializaion took ${sw.elapsed(MILLISECONDS)} ms"

        try {
            logger.info "Starting execution of optimization task"
            sw.reset().start()
            task.execute()
            sw.stop()
            Archive archive = task.getInstance(Archive.class)

            logger.info "Execution took ${sw.elapsed(MILLISECONDS)} ms"
            logger.info "${archive.size()} optimal values found"
            for (individual in archive) {
                println individual.getGenotype()
                println individual.getPhenotype()
                println individual.getObjectives()
            }

        } catch (Exception e) {
            e.printStackTrace()
        }
        finally {
            task.close()
        }
    }


    @Log4j('logger')
    static class Opt4jLoggingListener implements OptimizerIterationListener, IndividualStateListener {
        int evaluatedCount = 0

        @Override
        void iterationComplete(int iteration) {
            logger.info "Iteration $iteration completed"
        }

        @Override
        void inidividualStateChanged(Individual individual) {
            if (individual.evaluated) {
                //++evaluatedCount % 10 == 0 ? logger.info("$evaluatedCount individuals evaluated") : null
                logger.info("${++evaluatedCount} individuals evaluated")
            }
        }
    }
}
