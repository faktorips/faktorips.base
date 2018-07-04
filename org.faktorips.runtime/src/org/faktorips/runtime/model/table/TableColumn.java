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
import org.faktorips.runtime.model.type.DocumentationKind;
import org.faktorips.runtime.model.type.ModelElement;
import org.faktorips.runtime.model.type.read.SimpleTypePartsReader;
import org.faktorips.runtime.model.type.read.SimpleTypePartsReader.ModelElementCreator;
import org.faktorips.runtime.model.type.read.SimpleTypePartsReader.NameAccessor;
import org.faktorips.runtime.model.type.read.SimpleTypePartsReader.NamesAccessor;
import org.faktorips.runtime.util.MessagesHelper;

/**
 * Description of one column of a runtime {@linkplain ITable table}.
 */
public class TableColumn extends ModelElement {

    private final Class<?> datatype;

    private final Method getterMethod;

    private final TableStructure tableStructure;

    protected TableColumn(TableStructure tableStructure, String name, Class<?> datatype, Method getterMethod) {
        super(name, getterMethod.getAnnotation(IpsExtensionProperties.class));
        this.tableStructure = tableStructure;
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

    protected static LinkedHashMap<String, TableColumn> createModelsFrom(TableStructure tableStructure,
            Class<? extends ITable<?>> tableObjectClass,
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
        ModelElementCreator<TableColumn> createTableColumn = new ModelElementCreator<TableColumn>() {
            @Override
            public TableColumn create(ModelElement modelElement, String name, Method getterMethod) {
                return new TableColumn((TableStructure)modelElement, name, getterMethod.getReturnType(), getterMethod);
            }
        };
        // @formatter:off
        SimpleTypePartsReader<TableColumn, IpsTableStructure, IpsTableColumn> partsReader = new SimpleTypePartsReader<TableColumn, IpsTableStructure, IpsTableColumn>(
                parentAnnotation,
                getNamesOfPartsFromParentAnnotation,
                childAnnotation,
                getNameOfPartFromChildAnnotation,
                createTableColumn);
        return partsReader.createParts(tableObjectClass, tableRowClass, tableStructure);
        // @formatter:on
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
