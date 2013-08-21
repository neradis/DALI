package de.uni_leipzig.mack.persistence.instrumentation

import com.db4o.instrumentation.core.ClassFilter
import groovy.transform.CompileStatic

import java.lang.annotation.Annotation

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@CompileStatic
class Db4oIntrumentationFilter implements ClassFilter {
    boolean accept(Class<?> cls) {
        try {
            if (cls.is(null) || cls == Object.class) {
                false
            } else {
                hasAnnotation(cls) || accept(cls.superclass)
            }
        } catch (NullPointerException) {
            return false
        }
    }

    private boolean hasAnnotation(Class<?> cls) {
        cls.annotations.any { Annotation a -> a.annotationType().name == Db4oIntrument.class.name }
    }
}
