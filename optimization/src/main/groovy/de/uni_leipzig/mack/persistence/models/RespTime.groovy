package de.uni_leipzig.mack.persistence.models

import groovy.transform.CompileStatic
import groovy.transform.Immutable

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@CompileStatic
@Immutable
class RespTime {
    String uri
    int respTime
}
