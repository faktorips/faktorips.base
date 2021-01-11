/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
import org.faktorips.runtime.model.type.DocumentationKind;
import org.faktorips.runtime.model.type.ModelElement;
import org.faktorips.runtime.util.MessagesHelper;
import org.faktorips.runtime.util.StringBuilderJoiner;

/**
 * Description of a runtime {@linkplain ITable table's} name, {@linkplain TableStructureKind kind}
 * and {@linkplain TableColumn columns}.
 */
public class TableStructure extends ModelElement {

    public static final String KIND_NAME = "TableStructure";

    private TableStructureKind kind;
    private LinkedHashMap<String, TableColumn> columnModels;
    private List<String> columnNames;
    private final MessagesHelper messagesHelper;

    public TableStructure(Class<? extends ITable<?>> tableObjectClass) {
        super(tableObjectClass.getAnnotation(IpsTableStructure.class).name(),
                tableObjectClass.getAnnotation(IpsExtensionProperties.class));
        IpsTableStructure annotation = tableObjectClass.getAnnotation(IpsTableStructure.class);

        this.kind = annotation.type();
        this.columnNames = Arrays.asList(annotation.columns());

        Class<?> tableRowClass = (Class<?>)((ParameterizedType)tableObjectClass.getGenericSuperclass())
                .getActualTypeArguments()[0];
        this.columnModels = TableColumn.createModelsFrom(this, tableObjectClass, tableRowClass);
        messagesHelper = createMessageHelper(tableObjectClass.getAnnotation(IpsDocumented.class),
                tableObjectClass.getClassLoader());
    }

    /**
     * @return TableStructureKind indicating if the table is single content or multiple content
     */
    public TableStructureKind getKind() {
        return kind;
    }

    /**
     * @return columns of the table as {@link TableColumn}
     */
    public List<TableColumn> getColumns() {
        return new ArrayList<TableColumn>(columnModels.values());
    }

    /**
     * @param columnName name of the column in table
     * @return TableColumn of the column with the given name
     */
    public TableColumn getColumn(String columnName) {
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
     * @param tableRow an instance of the table row class matching the kind of the tableObjectClass
     * @param column model of the table column
     * @return the value of the table cell at in given tableRow and column
     * @see TableColumn#getValue(Object)
     */
    public Object getValue(Object tableRow, TableColumn column) {
        return column.getValue(tableRow);
    }

    /**
     * @see #getValue(Object, TableColumn)
     */
    public Object getValue(Object tableRow, String columnName) {
        return getValue(tableRow, getColumn(columnName));
    }

    /**
     * @return a list of all values in tableRow
     * @see #getValue(Object, TableColumn)
     */
    public List<Object> getValues(Object tableRow) {
        List<Object> values = new ArrayList<Object>();

        for (TableColumn column : columnModels.values()) {
            values.add(getValue(tableRow, column));
        }

        return values;
    }

    @Override
    protected MessagesHelper getMessageHelper() {
        return messagesHelper;
    }

    @Override
    protected String getMessageKey(DocumentationKind messageType) {
        return messageType.getKey(getName(), KIND_NAME, IpsStringUtils.EMPTY);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        sb.append(": ");
        sb.append(kind);
        sb.append("(");
        StringBuilderJoiner.join(sb, columnNames);
        sb.append(")");
        return sb.toString();
    }
}
