/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.tablestructure;

/**
 * A unique key is a list of key items that, given a value for each item, 
 * you can find exactly one row in the table is belongs to or no none.
 */
public interface IUniqueKey extends IKey {

    /**
     * The name of the unique key is the concatenation of it's items separated
     * by a comma and a space character (<code>", "</code>).
     *  
     * @see org.faktorips.devtools.core.model.IIpsElement#getName()
     */
    public String getName();
    
    /**
     * Returns true if the key contains any ranges.
     */
    public boolean containsRanges();

    /**
     * Returns true if the key contains any columns.
     */
    public boolean containsColumns();
    
    /**
     * Returns ture if the key contains only ranges.
     */
    public boolean containsRangesOnly();
    
}
