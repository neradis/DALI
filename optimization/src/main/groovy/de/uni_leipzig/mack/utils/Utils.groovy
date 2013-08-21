package de.uni_leipzig.mack.utils

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@CompileStatic
class Utils {

    static <T> BiMap<T, Integer> list2RankBiMap(List<T> list) {
        BiMap<T, Integer> ret = HashBiMap.create(list.size())
        list.eachWithIndex { T entry, int i -> ret.put(entry, i + 1) }
        ret
    }

    static double harmonicMean(List<Double> values) {
        def denom = values.collect({ double x -> 1d / x }).inject { double x, double y -> x + y }
        values.size() / denom
    }

    static double arithmeticMean(List<Double> values) {
        values.inject({ double x, double y -> x + y }) / values.size()
    }


    @TypeChecked
    static class NamedItiem<T> {
        T item
        String name

        NamedItiem(T item, String name) {
            this.item = item
            this.name = name
        }

        T getItem() {
            return item
        }

        String getName() {
            return name
        }
    }
}
