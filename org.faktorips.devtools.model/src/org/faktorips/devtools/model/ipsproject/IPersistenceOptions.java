/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsproject;

/**
 * Persistence specific properties that can be configured for an IPS project (more specifically its
 * IpsProjectProperties).
 * <p>
 * These include maximum and minimum table name lengths (as well as column name lengths), and the
 * strategies used for deriving table/column names from arbitrary strings.
 * 
 * @author Roman Grutza
 */
public interface IPersistenceOptions {

    String XML_TAG_NAME = "PersistenceOptions"; //$NON-NLS-1$

    String MAX_TABLE_NAME_LENGTH_ATTRIBUTENAME = "maxTableNameLength"; //$NON-NLS-1$
    String MAX_COLUMN_NAME_LENGTH_ATTRIBUTENAME = "maxColumnNameLength"; //$NON-NLS-1$
    String ALLOW_LAZY_FETCH_FOR_SINGLE_VALUED_ASSOCIATIONS = "allowLazyFetchForSingleValuedAssociations"; //$NON-NLS-1$

    String MAX_TABLE_COLUMN_SCALE = "maxTableColumnScale"; //$NON-NLS-1$
    String MAX_TABLE_COLUMN_PRECISION = "maxTableColumnPrecision"; //$NON-NLS-1$
    String MAX_TABLE_COLUMN_SIZE = "maxTableColumnSize"; //$NON-NLS-1$

    /**
     * Returns the maximum length allowed for a valid database table name.
     */
    int getMaxTableNameLength();

    /**
     * Sets the maximum length allowed for a valid database table name.
     */
    void setMaxTableNameLength(int length);

    /**
     * Returns the maximum length allowed for a valid database table column name.
     */
    int getMaxColumnNameLenght();

    /**
     * Sets the maximum length allowed for a valid database table column name.
     */
    void setMaxColumnNameLength(int length);

    /**
     * Returns the strategy used for naming database tables.
     */
    ITableNamingStrategy getTableNamingStrategy();

    /**
     * Sets the strategy used for naming database tables.
     */
    void setTableNamingStrategy(ITableNamingStrategy newStrategy);

    /**
     * Returns the strategy used for naming database table columns.
     */
    ITableColumnNamingStrategy getTableColumnNamingStrategy();

    /**
     * Sets the strategy used for naming database table columns.
     */
    void setTableColumnNamingStrategy(ITableColumnNamingStrategy newStrategy);

    /**
     * Returns if the lazy fetching of single value associations (to-one) is allowed or not.
     */
    boolean isAllowLazyFetchForSingleValuedAssociations();

    /**
     * Sets if the lazy fetching of single value associations (to-one) is allowed or not.
     */
    void setAllowLazyFetchForSingleValuedAssociations(boolean allowLazyFetchForSingleValuedAssociations);

    /**
     * Returns the maximum column size.
     */
    int getMaxTableColumnSize();

    /**
     * Returns the maximum column scale.
     */
    int getMaxTableColumnScale();

    /**
     * Returns the maximum column precision.
     */
    int getMaxTableColumnPrecision();

    /**
     * Returns the minimum column precision.
     */
    int getMinTableColumnPrecision();

    /**
     * Returns the minimum column scale.
     */
    int getMinTableColumnScale();

    /**
     * Returns the minimum column size.
     */
    int getMinTableColumnSize();

    /**
     * Sets the maximum column scale.
     */
    void setMaxTableColumnScale(int scale);

    /**
     * Sets the maximum column precision.
     */
    void setMaxTableColumnPrecision(int precision);

    /**
     * Sets the maximum column size.
     */
    void setMaxTableColumnSize(int size);
}
