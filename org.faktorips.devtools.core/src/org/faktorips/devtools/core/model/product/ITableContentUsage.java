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

package org.faktorips.devtools.core.model.product;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.product.IPropertyValue;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;

/**
 * 
 * @author Thorsten Guenther
 */
public interface ITableContentUsage extends IIpsObjectPart, IPropertyValue {

    public static final String TAG_NAME = "TableContentUsage"; //$NON-NLS-1$
    
    public static final String PROPERTY_STRUCTURE_USAGE = "structureUsage"; //$NON-NLS-1$
    
    public static final String PROPERTY_TABLE_CONTENT = "tableContentName"; //$NON-NLS-1$
    
    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "TABLECONTENT-USAGE"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the given structure usage is not known.
     */
    public final static String MSGCODE_UNKNOWN_STRUCTURE_USAGE = MSGCODE_PREFIX + "UnknownStructureUsage"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the given table content was not found.
     */
    public final static String MSGCODE_UNKNOWN_TABLE_CONTENT = MSGCODE_PREFIX + "UnknownTableContent"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the given table content does not match the needs of the structure
     */
    public final static String MSGCODE_INVALID_TABLE_CONTENT = MSGCODE_PREFIX + "InvalidTableContent"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the product cmpt type was not found.
     */
    public final static String MSGCODE_NO_TYPE = MSGCODE_PREFIX + "NoProductCmptType"; //$NON-NLS-1$

    /**
     * Set the rolename of the table structure usage implemented by this content usage.
     * 
     * @param structureUsage The rolename of the table structure usage referred to by this content usage.
     */
    public void setStructureUsage(String structureUsageRolename);
    
    /**
     * @return The rolename of the table structure usage implemented by this content usage.
     */
    public String getStructureUsage();
    
    /**
     * Set the name of the table conent used by this table content usage.
     * 
     * @param tableContentName The fully quallified name of the used table content.
     */
    public void setTableContentName(String tableContentName);
    
    /**
     * @return The fully quallified name of the used table content.
     */
    public String getTableContentName();
    
    /**
     * Returns the table contents which is related or <code>null</code> if the table contents
     * can't be found.
     * 
     * @throws CoreException if an error occurs while searching for the table contents.
     */
    public ITableContents findTableContents() throws CoreException;

    /**
     * Returns the related table structure usage or <code>null</code> if the table contents
     * can't be found.
     * 
     * @param project The project which ips object path is used for the searched.
     * This is not neccessarily the project this type is part of. 
     * 
     * @throws CoreException if an error occurs while searching for the table structure usage.
     * @throws NullPointerException if ipsProject is <code>null</code>.
     */
    public ITableStructureUsage findTableStructureUsage(IIpsProject ipsProject) throws CoreException;

}
