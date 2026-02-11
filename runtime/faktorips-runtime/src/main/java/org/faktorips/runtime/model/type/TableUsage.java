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
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IProductObject;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.model.annotation.IpsTableUsage;
import org.faktorips.runtime.model.table.TableStructure;

/**
 * Describes the model information for a table usage.
 */
public class TableUsage extends TypePart {

    public static final String MSGCODE_REQUIRED_TABLE_IS_NOT_SET = "REQUIRED_TABLE_IS_NOT_SET";
    public static final String MSGKEY_REQUIRED_TABLE_IS_NOT_SET = "TableUsage.RequiredTableIsNotSet";

    public static final String PROPERTY_REQUIRED_TABLE = "requiredTable";

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
     * Returns the name of the table for the given product component that references a table usage.
     * If this table usage is changing over time (resides in the generation) the date is used to
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
     * @since 24.7
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

    /**
     * Sets the name of the table for the given product component that references a table usage. If
     * this table usage is changing over time (resides in the generation) the date is used to
     * retrieve the correct generation. If the date is <code>null</code> the latest generation is
     * used. If the table usage is not changing over time the date will be ignored.
     *
     *
     * @param tableName The name of the table for this table usage in the product component
     * @param productComponent The product component that holds the table instance
     * @param effectiveDate the date to determine the product component generation. If
     *            <code>null</code> the latest generation is used. Is ignored if the table usage
     *            configuration is not changing over time.
     *
     * @since 24.7
     */
    public void setTableName(String tableName, IProductComponent productComponent, Calendar effectiveDate) {
        setTableName(tableName, getRelevantProductObject(productComponent, effectiveDate, isChangingOverTime()));
    }

    /**
     * Set the table name using a product component generation.
     * <p>
     * If the table usage is not changing over time, the underlying product component is used
     * directly; otherwise, the provided generation is used.
     * </p>
     *
     * @param tableName the name of the table for this table usage in the product component
     * @param generation the product component generation to determine the target product component
     *            for setting the table name
     * @since 25.1
     */
    public void setTableName(String tableName, IProductComponentGeneration generation) {
        setTableName(tableName, getRelevantProductObject(generation, isChangingOverTime()));
    }

    private void setTableName(String tableName, IProductObject productObject) {
        try {
            String setterNameForTableName = "set" + getter.getName().substring(3) + "Name";
            Method setterForName = getter.getDeclaringClass().getDeclaredMethod(setterNameForTableName, String.class);
            setterForName.invoke(productObject, tableName);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new IllegalArgumentException(
                    String.format("Could not set table name for table usage %s on product component %s.",
                            getName(),
                            productObject instanceof IProductComponentGeneration gen ? gen.getProductComponent()
                                    : productObject),
                    e);
        }
    }

    /**
     * Determines whether the associated table usage is marked as required.
     *
     * <p>
     * This method retrieves the {@link IpsTableUsage} annotation from the getter method and checks
     * the {@code required} attribute of the annotation.
     *
     * @return {@code true} if the {@code required} attribute of the {@link IpsTableUsage}
     *             annotation is set to {@code true}; otherwise, {@code false}.
     *
     * @since 25.1
     */
    public boolean isRequired() {
        return getter.getAnnotation(IpsTableUsage.class).required();
    }

    @Override
    public void validate(MessageList list,
            IValidationContext context,
            IProductComponent productComponent,
            Calendar effectiveDate) {

        ResourceBundle messages = ResourceBundle.getBundle(TableUsage.class.getName(), context.getLocale());
        String formatString = String.format(messages.getString(MSGKEY_REQUIRED_TABLE_IS_NOT_SET),
                getLabel(context.getLocale()), productComponent.getId());

        if (isRequired()) {
            if (IpsStringUtils.isEmpty(getTableName(productComponent, effectiveDate))) {
                list.newError(MSGCODE_REQUIRED_TABLE_IS_NOT_SET, formatString,
                        new ObjectProperty(this, PROPERTY_REQUIRED_TABLE),
                        new ObjectProperty(productComponent, getName()));
            }
        }

    }

    @Override
    public boolean isChangingOverTime() {
        return IProductComponentGeneration.class.isAssignableFrom(getter.getDeclaringClass());
    }

    /**
     *
     * @deprecated for removal since 25.1. Use {@link #getTableStructures()} instead.
     *
     * @return the model for the table structure referenced in this table usage.
     * @throws UnsupportedOperationException if this table usage uses multiple table structures.
     */
    @Deprecated(forRemoval = true, since = "25.1")
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

    /**
     * {@return a list of models for the table structures referenced in this table usage.}
     */
    public List<TableStructure> getTableStructures() {
        Class<? extends ITable<?>>[] tableClasses = getter.getAnnotation(IpsTableUsage.class).tableClasses();

        return Arrays.stream(tableClasses)
                .map(IpsModel::getTableStructure)
                .toList();
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
