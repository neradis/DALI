package de.uni_leipzig.mack.evaluation

import aksw.org.doodle.dataset.VectorDescription
import com.google.common.base.Objects
import de.uni_leipzig.mack.persistence.instrumentation.Db4oIntrument
import groovy.transform.CompileStatic

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */

@CompileStatic
@Db4oIntrument
public class KnowledgeBase implements Comparable<KnowledgeBase> {
    private final String label
    private final VectorDescription vectorDescription

    KnowledgeBase(String label, VectorDescription vectorDescription) {
        this.label = label
        this.vectorDescription = vectorDescription
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true
        if (o == null || getClass() != o.getClass()) return false

        KnowledgeBase that = (KnowledgeBase) o

        if (getLabel() != null ? !getLabel().equals(that.getLabel()) : that.getLabel() != null) return false

        return true
    }

    @Override
    public int hashCode() {
        return getLabel() != null ? getLabel().hashCode() : 0
    }

    @Override
    public String toString() {
        def vd = getVectorDescription()?.features.size().with { int n -> " has $n features" } ?: '[no feature vector]'
        return Objects.toStringHelper(this).add("label", getLabel()).add("feature vector", vd).toString()
    }

    public String getLabel() {
        return label
    }

    public VectorDescription getVectorDescription() {
        return vectorDescription
    }

    @Override
    public int compareTo(KnowledgeBase o) {
        return getLabel().compareTo(o.getLabel())
    }
}
