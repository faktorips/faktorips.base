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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.faktorips.runtime.ITable;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.model.annotation.IpsTableColumn;
import org.faktorips.runtime.model.annotation.IpsTableStructure;
import org.faktorips.runtime.modeltype.internal.AbstractModelElement;
import org.faktorips.runtime.modeltype.internal.DocumentationType;
import org.faktorips.runtime.util.MessagesHelper;

/**
 * Description of one column of a runtime {@linkplain ITable table}.
 */
public class TableColumnModel extends AbstractModelElement {

    private final Class<?> datatype;

    private final Method getterMethod;

    private final TableModel tableModel;

    protected TableColumnModel(TableModel tableModel, String name, Class<?> datatype, Method getterMethod) {
        super(name, getterMethod.getAnnotation(IpsExtensionProperties.class));
        this.tableModel = tableModel;
        this.datatype = datatype;
        this.getterMethod = getterMethod;
    }

    /**
     * @return the class for this column's values
     */
    public Class<?> getDatatype() {
        return datatype;
    }

    protected Method getGetterMethod() {
        return getterMethod;
    }

    /**
     * @return the value of this column in the given row
     */
    public Object getValue(Object row) {
        try {
            return getGetterMethod().invoke(row);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Can't get value for column \"" + getName() + "\"", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Can't get value for column \"" + getName() + "\"", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Can't get value for column \"" + getName() + "\"", e);
        }
    }

    protected static LinkedHashMap<String, TableColumnModel> createModelsFrom(TableModel tableModel,
            List<String> declaredColumnNames,
            Class<?> tableRowClass) {

        HashMap<String, TableColumnModel> columnModels = new HashMap<String, TableColumnModel>();

        for (Method method : tableRowClass.getMethods()) {
            if (method.isAnnotationPresent(IpsTableColumn.class)) {
                IpsTableColumn annotation = method.getAnnotation(IpsTableColumn.class);

                String columnName = annotation.name();

                if (declaredColumnNames.contains(columnName)) {
                    Class<?> returntype = method.getReturnType();
                    columnModels.put(columnName, new TableColumnModel(tableModel, columnName, returntype, method));
                } else {
                    throw new IllegalStateException("\"" + columnName + "\" is not listed as column in the @"
                            + IpsTableStructure.class.getSimpleName() + " annotation");
                }
            }
        }

        // as no duplicates are allowed, this should guaranty the equality
        if (columnModels.size() == declaredColumnNames.size()) {
            return sortHashMapByKeys(declaredColumnNames, columnModels);
        } else {
            // in case declaredColumnNames is a fix size list, remove is not supported
            LinkedList<String> declaredColumnNamesCopy = new LinkedList<String>(declaredColumnNames);

            declaredColumnNamesCopy.removeAll(columnModels.keySet());
            String s = "";
            if (declaredColumnNamesCopy.size() > 1) {
                s = "s";
            }
            throw new IllegalStateException("No getter method" + s + " found for annotated column" + s + " \""
                    + IpsStringUtils.join(declaredColumnNamesCopy, "\", \"") + "\"");
        }
    }

    private static LinkedHashMap<String, TableColumnModel> sortHashMapByKeys(List<String> keys,
            HashMap<String, TableColumnModel> columnModels) {

        LinkedHashMap<String, TableColumnModel> results = new LinkedHashMap<String, TableColumnModel>();
        for (String key : keys) {
            results.put(key, columnModels.get(key));
        }

        return results;
    }

    @Override
    protected String getMessageKey(DocumentationType messageType) {
        return messageType.getKey(tableModel.getName(), getName());
    }

    @Override
    protected MessagesHelper getMessageHelper() {
        return tableModel.getMessageHelper();
    }
}
