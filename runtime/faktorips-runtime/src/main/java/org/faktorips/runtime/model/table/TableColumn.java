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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;

import org.faktorips.runtime.ITable;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.model.annotation.IpsTableColumn;
import org.faktorips.runtime.model.annotation.IpsTableStructure;
import org.faktorips.runtime.model.type.Deprecation;
import org.faktorips.runtime.model.type.DocumentationKind;
import org.faktorips.runtime.model.type.ModelElement;
import org.faktorips.runtime.model.type.read.SimpleTypePartsReader;
import org.faktorips.runtime.util.MessagesHelper;

/**
 * Description of one column of a runtime {@linkplain ITable table}.
 */
public class TableColumn extends ModelElement {

    private final Class<?> datatype;

    private final Method getter;

    private final TableStructure tableStructure;

    protected TableColumn(TableStructure tableStructure, String name, Class<?> datatype, Method getter) {
        super(name, getter.getAnnotation(IpsExtensionProperties.class), Deprecation.of(getter));
        this.tableStructure = tableStructure;
        this.datatype = datatype;
        this.getter = getter;
    }

    /**
     * @return the class for this column's values
     */
    public Class<?> getDatatype() {
        return datatype;
    }

    protected Method getGetterMethod() {
        return getter;
    }

    /**
     * @return the value of this column in the given row
     */
    public Object getValue(Object row) {
        try {
            return getGetterMethod().invoke(row);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException("Can't get value for column \"" + getName() + "\"", e);
        }
    }

    protected static LinkedHashMap<String, TableColumn> createModelsFrom(TableStructure tableStructure,
            Class<? extends ITable<?>> tableObjectClass,
            Class<?> tableRowClass) {
        return new SimpleTypePartsReader<>(
                IpsTableStructure.class,
                IpsTableStructure::columns,
                IpsTableColumn.class,
                IpsTableColumn::name,
                (modelElement, name, getterMethod) -> new TableColumn((TableStructure)modelElement, name,
                        getterMethod.getReturnType(), getterMethod)).createParts(tableObjectClass, tableRowClass,
                                tableStructure);
    }

    @Override
    protected String getMessageKey(DocumentationKind messageType) {
        return messageType.getKey(tableStructure.getName(), TableStructure.KIND_NAME, getName());
    }

    @Override
    protected MessagesHelper getMessageHelper() {
        return tableStructure.getMessageHelper();
    }
}
