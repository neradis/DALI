package de.uni_leipzig.mack.evaluation

import com.db4o.ObjectContainer
import com.db4o.collections.ActivatableHashSet
import com.db4o.query.Predicate
import com.google.common.base.Optional
import de.uni_leipzig.mack.persistence.Db4o
import de.uni_leipzig.mack.persistence.LoadError
import de.uni_leipzig.mack.persistence.instrumentation.Db4oIntrument
import groovy.transform.TypeChecked

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@TypeChecked
@Db4oIntrument
class KnowledgeBasePool implements Set<KnowledgeBase> {
    protected String label

    @Delegate
    protected Set<KnowledgeBase> delegate

    protected KnowledgeBasePool(String label, Set<KnowledgeBase> delegate) {
        this.label = label
        this.delegate = delegate
        getDelegate() // for TA
    }

    static KnowledgeBasePool create(String label, Collection<KnowledgeBase> kbSet) {
        if (label.is(null)) {
            throw new IllegalArgumentException()
        }
        def activatableSet = new ActivatableHashSet<KnowledgeBase>(kbSet)
        new KnowledgeBasePool(label, activatableSet)
    }

    static Optional<KnowledgeBasePool> loadForLabel(String label) {
        Db4o.instance.readOnlyOperation { ObjectContainer oc ->
            def result = oc.query(new Predicate<KnowledgeBasePool>() {
                @Override
                boolean match(KnowledgeBasePool candidate) {
                    return candidate.getLabel() == label
                }
            })

            switch (result.size()) {
                case 0: return Optional.absent()
                case 1: return Optional.of(result.first())
                default: throw new LoadError("Ambiguous - multiple KBPools for $label")
            }
        }
    }

    String getLabel() {
        return label
    }

    protected Set<KnowledgeBase> getDelegate() {
        return delegate
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        KnowledgeBasePool that = (KnowledgeBasePool) o

        if (getLabel() != that.getLabel()) return false

        return true
    }

    int hashCode() {
        return getLabel().hashCode()
    }
}