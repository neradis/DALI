package de.uni_leipzig.mack.optimization.gridsearch

import com.google.common.base.Optional
import com.google.inject.Inject
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.opt4j.core.genotype.DoubleGenotype
import org.opt4j.core.problem.Creator
import org.opt4j.core.start.Constant

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@CompileStatic
class GridSearchCreator implements Creator<DoubleGenotype> {
    protected int dimensions
    protected double stepSize
    protected GridSearchIterator iter

    @Inject
    GridSearchCreator(@Constant('dimensions') int dimensions, @Constant('step') double stepSize) {
        this.dimensions = dimensions
        this.stepSize = stepSize
        this.iter = new GridSearchIterator()
    }

    @Override
    DoubleGenotype create() {
        if (iter.hasNext()) {
            return iter.next()
        } else {
            throw new GridSearchExhaustedException()
        }
    }

    @CompileStatic
    protected class GridSearchIterator implements Iterator<DoubleGenotype> {
        ArrayList<Double> valueMemory = pointOfOrigin()
        Optional<DoubleGenotype> nextVal = Optional.of(genotypeFromList(valueMemory));


        @Override
        boolean hasNext() {
            nextVal.present || computeNextVal()
        }

        @Override
        DoubleGenotype next() {
            if (hasNext()) {
                def ret = nextVal.get()
                nextVal = Optional.absent()
                return ret
            } else {
                throw new NoSuchElementException()
            }
        }

        boolean computeNextVal() {
            assert !nextVal.present
            def dimCursor = valueMemory.listIterator(valueMemory.size())
            while (dimCursor.hasPrevious()) {
                def currentListEntry = dimCursor.previous()
                def nextListEntry = currentListEntry + stepSize
                if (nextListEntry > 1) {
                    dimCursor.set(0d) //reset values to start for the current dimension
                    continue //we need to increase the following dimension
                } else {
                    dimCursor.set(nextListEntry)
                    nextVal = Optional.of(genotypeFromList(valueMemory))
                    return true
                }
            }
            return false
        }

        @CompileStatic(TypeCheckingMode.SKIP)
        DoubleGenotype genotypeFromList(List<Double> values) {
            def gene = new DoubleGenotype()
            gene.ensureCapacity(values.size())

            values.eachWithIndex { double d, int i ->
                if (i < gene.size()) {
                    gene.set(i, d)
                } else {
                    assert i == gene.size()
                    gene.add(d)
                }
            }
            assert gene.size() == values.size()
            return gene
        }

        ArrayList<Double> pointOfOrigin() {
            def l = new ArrayList<Double>(dimensions)
            while (l.size() < dimensions) {
                l.add(0d)
            }
            assert l.size() == dimensions
            assert l.each { double d -> d == 0d }
            return l
        }

        @Override
        void remove() {
            throw new UnsupportedOperationException()
        }
    }
}
