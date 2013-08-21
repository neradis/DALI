package de.uni_leipzig.mack.optimization.gridsearch

import groovy.transform.TypeChecked
import groovy.util.logging.Log4j
import org.opt4j.core.genotype.DoubleGenotype

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@TypeChecked
@Log4j('logger')
class GridSearchCreatorTest extends GroovyTestCase {

    void testCreate() {
        def genes = new LinkedList<DoubleGenotype>()
        shouldFail(GridSearchExhaustedException.class) {
            def creator = new GridSearchCreator(3i, 0.33d)
            while (true) {
                genes << creator.create()
            }
        }
        assert genes.size() == 64i
    }
}
