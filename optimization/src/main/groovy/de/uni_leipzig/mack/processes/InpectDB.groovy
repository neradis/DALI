package de.uni_leipzig.mack.processes

import com.db4o.ObjectContainer
import de.uni_leipzig.mack.evaluation.KnowledgeBasePool
import de.uni_leipzig.mack.persistence.Db4o
import groovy.transform.TypeChecked

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@TypeChecked
class InpectDB {


    static void main(String[] args) {
        Db4o.instance.readOnlyOperation { ObjectContainer oc ->
            def results = oc.query(KnowledgeBasePool.class)
            def x = results.first()
            println x
        }


    }

}
