/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.model.table;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.faktorips.runtime.ITable;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.model.annotation.IpsTableStructure;
import org.faktorips.runtime.modeltype.internal.AbstractModelElement;
import org.faktorips.runtime.modeltype.internal.DocumentationType;
import org.faktorips.runtime.util.MessagesHelper;

/**
 * Description of a runtime {@linkplain ITable table's} name, {@linkplain TableStructureType type}
 * and {@linkplain TableColumnModel columns}.
 */
public class TableModel extends AbstractModelElement {

    private TableStructureType type;
    private LinkedHashMap<String, TableColumnModel> columnModels;
    private List<String> columnNames;
    private final MessagesHelper messagesHelper;

    public TableModel(Class<? extends ITable> tableObjectClass) {
        super(tableObjectClass.getAnnotation(IpsTableStructure.class).name(), tableObjectClass
                .getAnnotation(IpsExtensionProperties.class));
        IpsTableStructure annotation = tableObjectClass.getAnnotation(IpsTableStructure.class);

        this.type = annotation.type();
        this.columnNames = Arrays.asList(annotation.columns());

        Class<?> tableRowClass = (Class<?>)((ParameterizedType)tableObjectClass.getGenericSuperclass())
                .getActualTypeArguments()[0];
        this.columnModels = TableColumnModel.createModelsFrom(this, columnNames, tableRowClass);
        messagesHelper = createMessageHelper(tableObjectClass.getAnnotation(IpsDocumented.class),
                tableObjectClass.getClassLoader());
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

    @Override
    protected MessagesHelper getMessageHelper() {
        return messagesHelper;
    }

    @Override
    protected String getMessageKey(DocumentationType messageType) {
        return messageType.getKey(getName(), IpsStringUtils.EMPTY);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        sb.append(": ");
        sb.append(type);
        sb.append("(");
        boolean first = true;
        for (String columnName : columnNames) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(columnName);
        }
        sb.append(")");
        return sb.toString();
    }
}
