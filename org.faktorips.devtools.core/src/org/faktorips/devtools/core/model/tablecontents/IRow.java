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

package org.faktorips.devtools.core.model.tablecontents;

import org.faktorips.devtools.core.model.IIpsObjectPart;

/**
 *
 */
public interface IRow extends IIpsObjectPart {
    
    /**
     * Returns the row number as string.
     *  
     * @see org.faktorips.devtools.core.model.IIpsElement#getName()
     */
    public String getName();
    
    /**
     * Returns the number of the row in the table. First row has number 0. 
     */
    public int getRowNumber();
    
    /**
     * Returns the value for the indicated column index.
     *  
     * @param column The column index. 
     * 
     * @throws IllegalArgumentException if the row does no contain a cell
     * for the indicated column index.
     */
    public String getValue(int column);
    
    /**
     * Sets the value for the indicated column index.
     *  
     * @param column	The column index.
     * @param newValue	The new value as string. 
     * 
     * @throws IndexOutOfBoundsException if the row does no contain a cell
     * for the indicated column index.
     */
    public void setValue(int column, String newValue);
    
}
