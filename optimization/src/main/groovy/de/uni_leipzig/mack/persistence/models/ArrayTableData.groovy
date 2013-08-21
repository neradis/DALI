package de.uni_leipzig.mack.persistence.models

import groovy.transform.CompileStatic

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@CompileStatic
class ArrayTableData<R, C, V> {

    String uuid
    R[] rowNames
    C[] columnNames
    V[][] cellData

    protected ArrayTableData(String uuid, R[] rowNames, C[] columnNames, V[][] cellData) {
        this.uuid = uuid
        this.rowNames = rowNames
        this.columnNames = columnNames
        this.cellData = cellData
    }

    static <R, C, V> ArrayTableData<R, C, V> create(String uuid, R[] rowNames, C[] columnNames, V[][] cellData) {
        if (uuid.is(null) || rowNames.size() != columnNames.size() || cellData.length != rowNames.size()
                || cellData[0].size() != columnNames.size()) {
            throw new IllegalArgumentException("check uuid non-null and matching dimensions for the arrays")
        }
        new ArrayTableData<R, C, V>(uuid, rowNames, columnNames, cellData)
    }

    void updateCellData(ArrayTableData<R, C, V> update) {
        if (!(Arrays.equals(getRowNames(), update.getRowNames()) &&
                Arrays.equals(getColumnNames(), update.getColumnNames()))) {
            throw new IllegalArgumentException()
        }
        for (row in 0..<getRowNames().length) {
            for (col in 0..<getColumnNames().length) {
                getCellData()[row][col] = update.getCellData()[row][col]
            }
        }
        setCellData(getCellData()) //explicit update for TA
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ArrayTableData that = (ArrayTableData) o

        if (uuid != that.uuid) return false

        return true
    }

    int hashCode() {
        return uuid.hashCode()
    }

    String getUuid() {
        return uuid
    }

    protected R[] getRowNames() {
        return rowNames
    }

    protected C[] getColumnNames() {
        return columnNames
    }

    protected V[][] getCellData() {
        return cellData
    }
}
