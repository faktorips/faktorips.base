/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.ipsproject;

/**
 * Persistence specific properties that can be configured for an IPS project (more specifically its
 * IpsProjectProperties).
 * 
 * @author Roman Grutza
 */
public interface IPersistenceOptions {

    public final static String XML_TAG_NAME = "PersistenceOptions";

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
}
