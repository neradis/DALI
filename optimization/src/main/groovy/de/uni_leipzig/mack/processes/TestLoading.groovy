package de.uni_leipzig.mack.processes

import aksw.org.doodle.dataset.Description
import aksw.org.doodle.dataset.VectorDescription
import aksw.org.doodle.engine.Engine
import groovy.transform.TypeChecked

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@TypeChecked
class TestLoading {

    static void main(String[] args) {
        def datasets = Engine.instance.datasets
        def lobidKey = datasets.keySet().grep({ String l -> l.contains('lobid') }).first()
        def count = datasets.get(lobidKey).with { Description d ->
            if (d instanceof VectorDescription) d.features.get('article')
        }

        new File('lobidArticleCounts').withWriterAppend { Writer w ->
            w.write(count + '\n')
        }
    }
}
