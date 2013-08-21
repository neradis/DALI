package de.uni_leipzig.mack.persistence.util

import com.db4o.ObjectContainer
import com.db4o.ObjectSet
import com.db4o.query.Predicate
import com.google.common.collect.ArrayTable
import com.google.common.collect.Table
import de.uni_leipzig.mack.persistence.Db4o
import de.uni_leipzig.mack.persistence.models.ArrayTableData
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@TypeChecked
class PersistableArrayTable<R, C, V> implements Table<R, C, V> {
    protected String id
    @Delegate
    protected ArrayTable<R, C, V> backing = null

    static <R, C, V> PersistableArrayTable<R, C, V> create(String id, Iterable<R> rowNames, Iterable<C> columnNames) {
        ArrayTable<R, C, V> backing = createArrayTable(rowNames, columnNames)
        new PersistableArrayTable<R, C, V>(id, backing)
    }

    static <R, C, V> PersistableArrayTable<R, C, V> loadForId(String id) {
        ArrayTableData<R, C, V> data
        Db4o.instance.readOnlyOperation { ObjectContainer oc ->
            ObjectSet<ArrayTableData> result = oc.query(new Predicate<ArrayTableData>() {

                @Override
                boolean match(ArrayTableData candidate) {
                    return candidate.getUuid() == id
                }
            })
            if (result.size() != 1) {
                throw new IllegalStateException('no data or ambiguous data')
            }
            data = result.first()
        }
        if (!(data.rowNames.each { Object o -> o instanceof R } &&
                data.columnNames.each { Object o -> o instanceof C })) {
            throw new IllegalStateException("instance of row or colums names are not of expected type")
        }
        for (row in 0..<data.rowNames.length) {
            for (col in 0..<data.columnNames.length) {
                if (!(data.cellData[row][col] instanceof V)) {
                    throw new IllegalStateException('data cell of unexpected type')
                }
            }
        }
        fromData(id, (ArrayTableData<R, C, V>) data)
    }

    static <R, C, V> PersistableArrayTable<R, C, V> fromData(String id, ArrayTableData<R, C, V> data) {
        def rowNames = Arrays.asList(data.rowNames)
        def columnNames = Arrays.asList(data.columnNames)
        ArrayTable<R, C, V> backing = createArrayTable(rowNames, columnNames)
        for (rowIndex in 0..<rowNames.size()) {
            for (colIndex in 0..<columnNames.size()) {
                backing.set(rowIndex, colIndex, data.cellData[rowIndex][colIndex])
            }
        }
        new PersistableArrayTable<R, C, V>(id, backing)
    }

    synchronized void save(Class<V> cellClass) {
        Db4o.instance.writeTransaction { ObjectContainer oc ->
            def result = oc.query(new Predicate<ArrayTableData>() {

                @Override
                boolean match(ArrayTableData candidate) {
                    return candidate.getUuid() == id
                }
            })
            if (result.size() > 1) {
                throw new IllegalStateException('ambiguous data in db')
            }
            if (result.empty) {
                def data = toData(cellClass)
                oc.store(data)
            } else {
                def dbData = result.first()
                dbData.updateCellData(toData(cellClass))
            }
        }
    }

    synchronized ArrayTableData<R, C, V> toData(Class<V> cellClass) {
        def rowNames = rowKeyList().toArray(new R[rowKeyList().size()])
        def columnNames = columnKeyList().toArray(new C[columnKeyList().size()])
        def cellData = toArray(cellClass)
        ArrayTableData.create(id, rowNames, columnNames, cellData)
    }

    String getId() {
        return id
    }

    protected PersistableArrayTable(String id, ArrayTable<R, C, V> backing) {
        if (id.is(null)) throw new IllegalArgumentException()
        this.id = id
        this.backing = backing
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    private static <R, C, V> ArrayTable<R, C, V> createArrayTable(Iterable<R> r, Iterable<C> c) {
        ArrayTable.create(r, c)
    }
}
