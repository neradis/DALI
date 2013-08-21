package de.uni_leipzig.mack.persistence.util

import com.db4o.ObjectContainer as OC
import com.db4o.query.Predicate
import de.uni_leipzig.mack.config.Environment
import de.uni_leipzig.mack.persistence.Db4o
import de.uni_leipzig.mack.persistence.models.ArrayTableData
import groovy.transform.TypeChecked

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@TypeChecked
class PersistableArrayTableTest extends GroovyTestCase {

    void testLoadForId() {
        initTestData()
        def table = PersistableArrayTable.loadForId('_test0')
        assert table.size() == 64
        assert table.get('C', 3) == 'C3'
    }

    void testSaveNew() {
        initTestData()
        def newTable = makeTestTable(1)
        newTable.save(String.class)
        db4o.reopenContainer()
        def loadedTable = PersistableArrayTable.loadForId('_test1')
        assert loadedTable.size() == 64
        assert loadedTable.get('E', 2) == 'E2'

    }

    void testSaveUpdating() {
        initTestData()
        def table = PersistableArrayTable.loadForId('_test0')
        assert table.size() == 64
        assert table.get('C', 3) == 'C3'
        table.put('C', 3, 'update')
        table.save(String.class)
        db4o.reopenContainer()
        def loadedTable = PersistableArrayTable.loadForId('_test0')
        assert loadedTable.size() == 64
        assert loadedTable.get('C', 2) == 'C2'
        assert loadedTable.get('C', 3) == 'update'
    }


    void initTestData() {
        db4o.writeTransaction { OC oc ->
            def result = oc.query(TEST_DATA_PREDICATE)
            for (atb in result) {
                oc.delete(atb)
            }
        }
        db4o.readOnlyOperation { OC oc ->
            assert oc.query(TEST_DATA_PREDICATE).empty
        }
        def table = makeTestTable(0)
        table.save(String.class)
    }

    protected Db4o getDb4o() {
        if (Environment.byProperty() == Environment.PRODUCTION) {
            throw new IllegalStateException('Bad idea to test with production data')
        }
        Db4o.instance
    }

    protected PersistableArrayTable<Character, Integer, String> makeTestTable(int k) {
        PersistableArrayTable<Character, Integer, String> table =
            PersistableArrayTable.create("_test$k", ('A'..'Z')[0..<8], (1..8))
        for (r in table.rowKeySet()) {
            for (c in table.columnKeySet()) {
                table.put(r, c, "$r$c".toString())
            }
        }
        return table
    }

    protected final static Test TEST_DATA_PREDICATE = new Test()
    protected static class Test extends Predicate<ArrayTableData> {

        @Override
        boolean match(ArrayTableData candidate) {
            return candidate.uuid.startsWith('_test')
        }
    }

    /*protected final static Predicate<ArrayTableData> TEST_DATA_PREDICATE = new Predicate<ArrayTableData>() {

        @Override
        boolean match(ArrayTableData candidate) {
            return candidate.uuid.startsWith('_test')
        }
    }*/
}
