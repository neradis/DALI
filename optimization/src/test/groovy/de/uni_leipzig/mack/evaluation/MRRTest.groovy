package de.uni_leipzig.mack.evaluation

import com.google.common.collect.ImmutableList
import groovy.transform.TypeChecked

import static de.uni_leipzig.mack.TestUtils.createResultRelevancePair as resRelPair

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@TypeChecked
class MRRTest extends GroovyTestCase{
    public static final String DBPEDIA = 'dbpedia'
    public static final String GENOAMES = 'genoames'
    public static final String RKB_EXPLORER = 'rkb_xplorer'
    public static final String TALIS = 'talis'
    @Lazy protected Collection<ResultRelevancePair> rrPairs = ImmutableList.of(
            resRelPair([[DBPEDIA, GENOAMES, RKB_EXPLORER], [RKB_EXPLORER]]),
            resRelPair([[DBPEDIA, GENOAMES, RKB_EXPLORER], [GENOAMES]]),
            resRelPair([[DBPEDIA, GENOAMES, RKB_EXPLORER], [GENOAMES, DBPEDIA]]),
            resRelPair([[DBPEDIA, GENOAMES, RKB_EXPLORER], [TALIS]]))

    void testCompute() {
        def mrrAny =new MRRanyRelevant()
        def mrrBest =  new MRRmostRelevant()
        assertEquals(11d / 24d, mrrAny.compute(rrPairs.iterator()), 0.000001d)
        assertEquals( 8d / 24d, mrrBest.compute(rrPairs.iterator()), 0.000001d)
    }
}
