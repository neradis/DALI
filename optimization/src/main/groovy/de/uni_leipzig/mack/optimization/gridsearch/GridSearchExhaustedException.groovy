package de.uni_leipzig.mack.optimization.gridsearch

import groovy.transform.CompileStatic

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@CompileStatic
class GridSearchExhaustedException extends RuntimeException {
    GridSearchExhaustedException() {
        super()
    }

    GridSearchExhaustedException(String message) {
        super(message)
    }

    GridSearchExhaustedException(String message, Throwable cause) {
        super(message, cause)
    }

    GridSearchExhaustedException(Throwable cause) {
        super(cause)
    }
}