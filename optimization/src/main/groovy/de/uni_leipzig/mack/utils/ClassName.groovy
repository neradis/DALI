package de.uni_leipzig.mack.utils

import groovy.transform.TypeChecked

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@TypeChecked
class ClassName implements CharSequence {
    @Delegate String name

    ClassName(String name) {
        if (name.is(null)) {
            throw new IllegalArgumentException()
        }
        this.name = name
    }

    static ClassName forClass(Class c) {
        new ClassName(c.name)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ClassName className = (ClassName) o

        if (name != className.name) return false

        return true
    }

    int hashCode() {
        return name.hashCode()
    }
}
