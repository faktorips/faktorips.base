/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.modeltype.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.model.Models;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.model.annotation.IpsTableUsage;
import org.faktorips.runtime.model.table.TableModel;
import org.faktorips.runtime.modeltype.ITableUsageModel;

public class TableUsageModel extends ModelPart implements ITableUsageModel {

    private final Method getter;

    public TableUsageModel(ModelType parent, Method getterMethod) {
        super(getterMethod.getAnnotation(IpsTableUsage.class).name(), parent, getterMethod
                .getAnnotation(IpsExtensionProperties.class));
        getter = getterMethod;
    }

    @Override
    public ITable getTable(IProductComponent productComponent, Calendar effectiveDate) {
        try {
            return (ITable)getter
                    .invoke(getRelevantProductObject(productComponent, effectiveDate, isChangingOverTime()));
        } catch (IllegalAccessException e) {
            throw getterError(productComponent, e);
        } catch (InvocationTargetException e) {
            throw getterError(productComponent, e);
        } catch (SecurityException e) {
            throw getterError(productComponent, e);
        }
    }

    private boolean isChangingOverTime() {
        return IProductComponentGeneration.class.isAssignableFrom(getter.getDeclaringClass());
    }

    @Override
    public TableModel getTableModel() {
        Class<? extends ITable> tableClass = getter.getReturnType().asSubclass(ITable.class);

        if (tableClass.equals(ITable.class)) {
            throw new UnsupportedOperationException("Cannot create new TableModel as the table usage " + getName()
                    + " uses multiple table structures.");
        }

        return Models.getTableModel(tableClass);
    }

    private IllegalArgumentException getterError(IProductComponent source, Exception e) {
        return new IllegalArgumentException(String.format("Could not get table %s on product component %s.", getName(),
                source), e);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        sb.append(": ");
        sb.append(getter.getReturnType());
        return sb.toString();
    }
}
