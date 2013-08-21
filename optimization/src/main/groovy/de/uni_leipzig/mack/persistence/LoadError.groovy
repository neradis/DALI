package de.uni_leipzig.mack.persistence
/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */

class LoadError extends RuntimeException {

    LoadError() {
    }

    LoadError(String message) {
        super(message)
    }

    LoadError(String message, Throwable cause) {
        super(message, cause)
    }
}
