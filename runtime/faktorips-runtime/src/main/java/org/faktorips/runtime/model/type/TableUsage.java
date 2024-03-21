/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.model.annotation.IpsTableUsage;
import org.faktorips.runtime.model.table.TableStructure;

/**
 * Describes the model information for a table usage.
 */
public class TableUsage extends TypePart {

    private final Method getter;

    public TableUsage(Type parent, Method getter) {
        super(getter.getAnnotation(IpsTableUsage.class).name(), parent,
                getter.getAnnotation(IpsExtensionProperties.class), Deprecation.of(getter));
        this.getter = getter;
    }

    /**
     * Returns the table the given product component references for this table usage. If this table
     * usage is changing over time (resides in the generation) the date is used to retrieve the
     * correct generation. If the date is <code>null</code> the latest generation is used. If the
     * table usage is not changing over time the date will be ignored.
     *
     *
     * @param productComponent The product component that holds the table instance
     * @param effectiveDate the date to determine the product component generation. If
     *            <code>null</code> the latest generation is used. Is ignored if the table usage
     *            configuration is not changing over time.
     *
     * @return The table instance hold by the product component and is identified by this table
     *             usage
     */
    public ITable<?> getTable(IProductComponent productComponent, Calendar effectiveDate) {
        try {
            return (ITable<?>)getter
                    .invoke(getRelevantProductObject(productComponent, effectiveDate, isChangingOverTime()));
        } catch (IllegalAccessException | InvocationTargetException | SecurityException e) {
            throw getterError(productComponent, e);
        }
    }

    /**
     * Returns the name of the table the given product component references for this table usage. If
     * this table usage is changing over time (resides in the generation) the date is used to
     * retrieve the correct generation. If the date is <code>null</code> the latest generation is
     * used. If the table usage is not changing over time the date will be ignored.
     *
     *
     * @param productComponent The product component that holds the table instance
     * @param effectiveDate the date to determine the product component generation. If
     *            <code>null</code> the latest generation is used. Is ignored if the table usage
     *            configuration is not changing over time.
     *
     * @return The name of the table for this table usage in the product component
     */
    public String getTableName(IProductComponent productComponent, Calendar effectiveDate) {
        String tableName = null;
        try {
            Method getterForName = getter.getDeclaringClass().getDeclaredMethod(getter.getName() + "Name");
            tableName = (String)getterForName
                    .invoke(getRelevantProductObject(productComponent, effectiveDate, isChangingOverTime()));
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new IllegalArgumentException(
                    String.format("Could not get table name for table usage %s on product component %s.",
                            getName(), productComponent),
                    e);
        }
        return tableName;
    }

    private boolean isChangingOverTime() {
        return IProductComponentGeneration.class.isAssignableFrom(getter.getDeclaringClass());
    }

    /**
     * @return the model for the table structure referenced in this table usage.
     * @throws UnsupportedOperationException if this table usage uses multiple table structures.
     */
    public TableStructure getTableStructure() {
        @SuppressWarnings("unchecked")
        Class<? extends ITable<?>> tableClass = (Class<? extends ITable<?>>)getter.getReturnType()
                .asSubclass(ITable.class);

        if (tableClass.equals(ITable.class)) {
            throw new UnsupportedOperationException("Cannot create new TableStructure as the table usage " + getName()
                    + " uses multiple table structures.");
        }

        return IpsModel.getTableStructure(tableClass);
    }

    private IllegalArgumentException getterError(IProductComponent source, Exception e) {
        return new IllegalArgumentException(
                String.format("Could not get table %s on product component %s.", getName(), source), e);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        sb.append(": ");
        sb.append(getter.getReturnType());
        return sb.toString();
    }
}
