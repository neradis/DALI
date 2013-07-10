package de.uni_leipzig.mack

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import groovy.transform.CompileStatic

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@CompileStatic
class Utils {

    static <T> BiMap<T, Integer> list2RankBiMap(List<T> list) {
        BiMap<T,Integer>  ret = HashBiMap.create(list.size())
        list.eachWithIndex { T entry, int i -> ret.put(entry, i+1) }
        ret
    }
}
