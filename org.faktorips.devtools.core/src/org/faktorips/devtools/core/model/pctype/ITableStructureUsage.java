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

package org.faktorips.devtools.core.model.pctype;


/**
 * 
 * @author Jan Ortmann
 */
public interface ITableStructureUsage {

    public final static String MSGCODE_PREFIX = "TableStructureUsage-";
    
    /**
     * Validation message code to indicate that the referenced table structure hasn't been found.
     */
    public final static String MSGCODE_TABLE_STRUCTURE_NOT_FOUND = MSGCODE_PREFIX + "TableStructureNotFound";
    
    /**
     * Validation message code to indicate that the role name is invalid.
     */
    public final static String MSGCODE_INVALID_ROLE_NAME = MSGCODE_PREFIX + "InvalidRoleName";

    /**
     * Validation message code to indicate that the the usgae does not reference at least one structure
     */
    public final static String MSGCODE_MUST_REFERENCE_AT_LEAST_1_TABLE_STRUCTURE = MSGCODE_PREFIX + "MustReferenceAtLeast1Structure";
    
    /**
     * Validation message code to indicate that the table structure usage has the same rolename as at least one other
     * usage in the supertype hierarchy.
     */ 
    public final static String MSGCODE_SAME_ROLENAME = MSGCODE_PREFIX + "SameRoleName";

    public String getRoleName();

    public void setRoleName(String s);

    public String[] getTableStructures();
}
