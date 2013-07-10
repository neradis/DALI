package de.uni_leipzig.mack

import de.uni_leipzig.mack.evaluation.KnowledgeBase
import de.uni_leipzig.mack.evaluation.ResultRelevancePair
import groovy.transform.TypeChecked

import static de.uni_leipzig.mack.Utils.list2RankBiMap

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@TypeChecked
class TestUtils {

    static ResultRelevancePair createResultRelevancePair(List<List<String>> queryAndRelListPair) {
        def kbQuRelPairs = queryAndRelListPair.collect{  List<String> nameList ->
            nameList.collect { String name -> new KnowledgeBase(name, null)}

        }

        new ResultRelevancePair(list2RankBiMap(kbQuRelPairs.get(0)),
                                list2RankBiMap(kbQuRelPairs.get(1)))
    }
}
