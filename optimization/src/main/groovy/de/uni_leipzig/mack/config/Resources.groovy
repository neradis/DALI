package de.uni_leipzig.mack.config

import groovy.transform.CompileStatic

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@CompileStatic
class Resources {

    static URL getResource(String name) {
        Thread.currentThread().contextClassLoader.getResource(name)
    }
}
