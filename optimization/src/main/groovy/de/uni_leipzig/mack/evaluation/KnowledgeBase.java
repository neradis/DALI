package de.uni_leipzig.mack.evaluation;

import aksw.org.doodle.dataset.VectorDescription;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */

class KnowledgeBase {
    private String uri;
    private VectorDescription vectorDescription;

    KnowledgeBase(String uri, VectorDescription vectorDescription) {
        this.uri = uri;
        this.vectorDescription = vectorDescription;
    }

    String getUri() {
        return uri;
    }

    VectorDescription getVectorDescription() {
        return vectorDescription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KnowledgeBase that = (KnowledgeBase) o;

        if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return uri != null ? uri.hashCode() : 0;
    }
}
