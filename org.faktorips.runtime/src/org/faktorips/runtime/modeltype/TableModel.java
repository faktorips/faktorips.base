/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.modeltype;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.faktorips.runtime.ITable;
import org.faktorips.runtime.modeltype.annotation.IpsTableStructure;
import org.faktorips.runtime.modeltype.annotation.TableStructureType;

/**
 * Description of a runtime {@linkplain ITable table's} name, {@linkplain TableStructureType type}
 * and {@linkplain TableColumnModel columns}.
 */
public class TableModel {

    private String name;
    private TableStructureType type;
    private LinkedHashMap<String, TableColumnModel> columnModels;
    private List<String> columnNames;

    public TableModel(Class<? extends ITable> tableObjectClass) {
        this.name = tableObjectClass.getName();

        IpsTableStructure annotation = tableObjectClass.getAnnotation(IpsTableStructure.class);

        this.type = annotation.type();
        this.columnNames = Arrays.asList(annotation.columns());

        Class<?> tableRowClass = (Class<?>)((ParameterizedType)tableObjectClass.getGenericSuperclass())
                .getActualTypeArguments()[0];
        this.columnModels = TableColumnModel.createModelsFrom(columnNames, tableRowClass);
    }

    /**
     * @return qualified name of the table structure class
     */
    public String getName() {
        return name;
    }

    /**
     * @return TableStructureType indicating if the table is single content or multiple content
     */
    public TableStructureType getType() {
        return type;
    }

    /**
     * @return columns of the table as {@link TableColumnModel}
     */
    public List<TableColumnModel> getColumns() {
        return new ArrayList<TableColumnModel>(columnModels.values());
    }

    /**
     * @param columnName name of the column in table
     * @return TableColumnModel of the column with the given name
     */
    public TableColumnModel getColumn(String columnName) {
        return columnModels.get(columnName);
    }

    /**
     * @return names of all columns
     */
    public List<String> getColumnNames() {
        return columnNames;
    }

    /**
     * This method retrieves the value of a table cell in a given row and column.
     * 
     * @param tableRow an instance of the table row class matching the type of the tableObjectClass
     * @param column model of the table column
     * @return the value of the table cell at in given tableRow and column
     * @see TableColumnModel#getValue(Object)
     */
    public Object getValue(Object tableRow, TableColumnModel column) {
        return column.getValue(tableRow);
    }

    /**
     * @see #getValue(Object, TableColumnModel)
     */
    public Object getValue(Object tableRow, String columnName) {
        return getValue(tableRow, getColumn(columnName));
    }

    /**
     * @return a list of all values in tableRow
     * @see #getValue(Object, TableColumnModel)
     */
    public List<Object> getValues(Object tableRow) {
        List<Object> values = new ArrayList<Object>();

        for (TableColumnModel column : columnModels.values()) {
            values.add(getValue(tableRow, column));
        }

        return values;
    }
}
