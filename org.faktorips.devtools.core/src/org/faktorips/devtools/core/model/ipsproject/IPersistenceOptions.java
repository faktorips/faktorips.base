/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.model.ipsproject;

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

    public final static String XML_TAG_NAME = "PersistenceOptions"; //$NON-NLS-1$

    public static final String MAX_TABLE_NAME_LENGTH_ATTRIBUTENAME = "maxTableNameLength"; //$NON-NLS-1$
    public static final String MAX_COLUMN_NAME_LENGTH_ATTRIBUTENAME = "maxColumnNameLength"; //$NON-NLS-1$
    public static final String ALLOW_LAZY_FETCH_FOR_SINGLE_VALUED_ASSOCIATIONS = "allowLazyFetchForSingleValuedAssociations"; //$NON-NLS-1$

    /**
     * Returns the maximum length allowed for a valid database table name.
     */
    public int getMaxTableNameLength();

    /**
     * Sets the maximum length allowed for a valid database table name.
     */
    public void setMaxTableNameLength(int length);

    /**
     * Returns the maximum length allowed for a valid database table column name.
     */
    public int getMaxColumnNameLenght();

    /**
     * Sets the maximum length allowed for a valid database table column name.
     */
    public void setMaxColumnNameLength(int length);

    /**
     * Returns the strategy used for naming database tables.
     */
    public ITableNamingStrategy getTableNamingStrategy();

    /**
     * Sets the strategy used for naming database tables.
     */
    public void setTableNamingStrategy(ITableNamingStrategy newStrategy);

    /**
     * Returns the strategy used for naming database table columns.
     */
    public ITableColumnNamingStrategy getTableColumnNamingStrategy();

    /**
     * Sets the strategy used for naming database table columns.
     */
    public void setTableColumnNamingStrategy(ITableColumnNamingStrategy newStrategy);

    /**
     * Returns if the lazy fetching of single value associations (to-one) is allowed or not.
     */
    public boolean isAllowLazyFetchForSingleValuedAssociations();

    /**
     * Sets if the lazy fetching of single value associations (to-one) is allowed or not.
     */
    public void setAllowLazyFetchForSingleValuedAssociations(boolean allowLazyFetchForSingleValuedAssociations);

}
