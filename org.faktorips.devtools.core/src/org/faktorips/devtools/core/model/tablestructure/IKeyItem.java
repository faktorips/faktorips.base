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
package org.faktorips.devtools.core.model.tablestructure;

/**
 * A key item is a part of a unique key. There are two kind of key items: columns and ranges.
 */
public interface IKeyItem {

    /**
     * Returns the item's name.
     */
    public String getName();

    /**
     * Returns the name for a parameter in a table access function. For columns this is the name of
     * the column, for ranges this parameter can be specified.
     */
    public String getAccessParameterName();

    /**
     * Returns the item's data type. For columns this is the column's data type and for ranges this
     * is the data type of the column if it's a one column range and the first column's data type if
     * it is a two column range.
     */
    public String getDatatype();

    /**
     * Returns the columns this item comprises.
     */
    public IColumn[] getColumns();

}
