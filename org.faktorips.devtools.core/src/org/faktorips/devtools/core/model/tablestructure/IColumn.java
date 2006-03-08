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

import org.faktorips.devtools.core.model.IIpsObjectPart;

/**
 *
 */
public interface IColumn extends IIpsObjectPart, IKeyItem {
    
    public final static String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$
    
    /**
     * Sets the column name.
     * 
     * @throws IllegalArgumentException if newName is <code>null</code>.
     */
    public void setName(String newName);
    
    /**
     * Sets the column's datatype.
     * 
     * @throws IllegalArgumentException if newDatatype is <code>null</code>.
     */
    public void setDatatype(String newDatatype);

}
