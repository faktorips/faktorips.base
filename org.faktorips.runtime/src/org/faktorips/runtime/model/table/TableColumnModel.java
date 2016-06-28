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
import java.util.LinkedHashMap;

import org.faktorips.runtime.ITable;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.model.annotation.IpsTableColumn;
import org.faktorips.runtime.model.annotation.IpsTableStructure;
import org.faktorips.runtime.modeltype.IModelElement;
import org.faktorips.runtime.modeltype.internal.AbstractModelElement;
import org.faktorips.runtime.modeltype.internal.DocumentationType;
import org.faktorips.runtime.modeltype.internal.read.SimpleTypeModelPartsReader;
import org.faktorips.runtime.modeltype.internal.read.SimpleTypeModelPartsReader.ModelElementCreator;
import org.faktorips.runtime.modeltype.internal.read.SimpleTypeModelPartsReader.NameAccessor;
import org.faktorips.runtime.modeltype.internal.read.SimpleTypeModelPartsReader.NamesAccessor;
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
            Class<? extends ITable> tableObjectClass,
            Class<?> tableRowClass) {
        Class<IpsTableStructure> parentAnnotation = IpsTableStructure.class;
        NamesAccessor<IpsTableStructure> getNamesOfPartsFromParentAnnotation = new NamesAccessor<IpsTableStructure>() {

            @Override
            public String[] getNames(IpsTableStructure annotation) {
                return annotation.columns();
            }
        };
        Class<IpsTableColumn> childAnnotation = IpsTableColumn.class;
        NameAccessor<IpsTableColumn> getNameOfPartFromChildAnnotation = new NameAccessor<IpsTableColumn>() {

            @Override
            public String getName(IpsTableColumn annotation) {
                return annotation.name();
            }
        };
        ModelElementCreator<TableColumnModel> createTableColumnModel = new ModelElementCreator<TableColumnModel>() {
            @Override
            public TableColumnModel create(IModelElement modelType, String name, Method getterMethod) {
                return new TableColumnModel((TableModel)modelType, name, getterMethod.getReturnType(), getterMethod);
            }
        };
        // @formatter:off
        SimpleTypeModelPartsReader<TableColumnModel, IpsTableStructure, IpsTableColumn> modelPartsReader = new SimpleTypeModelPartsReader<TableColumnModel, IpsTableStructure, IpsTableColumn>(
                parentAnnotation,
                getNamesOfPartsFromParentAnnotation,
                childAnnotation,
                getNameOfPartFromChildAnnotation,
                createTableColumnModel);
        return modelPartsReader.createParts(tableObjectClass, tableRowClass, tableModel);
        // @formatter:on
    }

    @Override
    protected String getMessageKey(DocumentationType messageType) {
        return messageType.getKey(tableModel.getName(), TableModel.KIND_NAME, getName());
    }

    @Override
    protected MessagesHelper getMessageHelper() {
        return tableModel.getMessageHelper();
    }
}
