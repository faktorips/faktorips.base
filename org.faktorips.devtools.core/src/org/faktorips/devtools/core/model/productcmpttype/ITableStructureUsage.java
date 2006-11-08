/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpttype;

import org.faktorips.devtools.core.model.IIpsObjectPart;

/**
 * View for the table structure usage.
 * 
 * @author JOerg Ortmann
 */
public interface ITableStructureUsage extends IIpsObjectPart {
    
    public final static String PROPERTY_ROLENAME = "roleName"; //$NON-NLS-1$
    
    public final static String PROPERTY_MANDATORY_TABLE_CONTENT = "mandatoryTableContent"; //$NON-NLS-1$
    
    /**
     * Returns the parent product cmpt type.
     */
    public IProductCmptType getProductCmptType();
    
    /**
     * Sets the role name.
     */
    public void setRoleName(String s);
    
    /**
     * Returns the role name.
     */
    public String getRoleName();  
    
    /**
     * Sets if the table content is mandatory for this table structure usage.
     */
    public void setMandatoryTableContent(boolean mandatoryTableContent);
    
    /**
     * Returns <code>true</code> if the table content is mandatory for this table structure usage,
     * otherwise <code>false</code>.
     */
    public boolean isMandatoryTableContent();
    
    /**
     * Returns all table structures this usage belongs to.<br>
     * Returns an empty array if no table structures are related by this usage object.
     */
    public String[] getTableStructures();
    
    /**
     * Moves the table structures identified by the indexes up or down by one position.
     * If one of the indexes is 0 (the first object), no object is moved up. 
     * If one of the indexes is the number of objects - 1 (the last object)
     * no object is moved down. 
     * 
     * @param indexes   The indexes identifying the table structures.
     * @param up        <code>true</code>, to move the table structures up, 
     * <false> to move them down.
     * 
     * @return The new indexes of the moved table structures.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify
     * a table structure.
     */
    public int[] moveTableStructure(int[] indexes, boolean up);
    
    /**
     * Adds the given table structure to the list of table structure this usage object specifies.
     */
    public void addTableStructure(String tableStructure);
    
    /**
     * Removes the given table structure from the list of table structures.
     */
    public void removeTableStructure(String tableStructure);
}
