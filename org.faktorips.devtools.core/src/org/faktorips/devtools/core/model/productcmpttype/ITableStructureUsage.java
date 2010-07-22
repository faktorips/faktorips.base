/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.productcmpttype;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

/**
 * Specification of table structure usage object.<br>
 * Specifies a usage of several table structure for a product component type.
 * 
 * @author Jan Ortmann
 */
public interface ITableStructureUsage extends IIpsObjectPart, IProdDefProperty {

    public final static String PROPERTY_ROLENAME = "roleName"; //$NON-NLS-1$

    public final static String PROPERTY_TABLESTRUCTURE = "tableStructure"; //$NON-NLS-1$

    public final static String PROPERTY_MANDATORY_TABLE_CONTENT = "mandatoryTableContent"; //$NON-NLS-1$

    public final static String MSGCODE_PREFIX = "TableStructureUsage-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the referenced table structure hasn't been found.
     */
    public final static String MSGCODE_TABLE_STRUCTURE_NOT_FOUND = MSGCODE_PREFIX + "TableStructureNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the role name is invalid.
     */
    public final static String MSGCODE_INVALID_ROLE_NAME = MSGCODE_PREFIX + "InvalidRoleName"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the the usage does not reference at least one
     * structure
     */
    public final static String MSGCODE_MUST_REFERENCE_AT_LEAST_1_TABLE_STRUCTURE = MSGCODE_PREFIX
            + "MustReferenceAtLeast1Structure"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the policy component type is not configurable by
     * product.
     */
    public final static String MSGCODE_POLICYCMPTTYPE_IS_NOT_CONFIGURABLE_BY_PRODUCT = MSGCODE_PREFIX
            + "PolicycmpttypeIsNotConfigurableByProduct"; //$NON-NLS-1$

    /**
     * Validation message code that identifies the validation rule that checks if role name is
     * already defined in the super type.
     */
    public final static String MSGCODE_ROLE_NAME_ALREADY_IN_SUPERTYPE = MSGCODE_PREFIX + "RoleNameAlreadyInSupertype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the table structure usage has the same role name as
     * at least one other usage in the supertype hierarchy.
     */
    public final static String MSGCODE_SAME_ROLENAME = MSGCODE_PREFIX + "SameRoleName"; //$NON-NLS-1$

    /**
     * Returns the role name.
     */
    public String getRoleName();

    /**
     * Sets the role name.
     */
    public void setRoleName(String s);

    /**
     * Returns all table structures this usage belongs to.<br>
     * Returns an empty array if no table structures are related by this usage object.
     */
    public String[] getTableStructures();

    /**
     * Returns <code>true</code> if this usage specified the given table structure as one the
     * structures that are allowed to use, otherwise <code>false</code>. Returns <code>false</code>
     * if tabelStructure is <code>null</code>.
     */
    public boolean isUsed(String tableStructure);

    /**
     * Adds the given table structure to the list of table structure this usage object specifies.<br>
     * If the table structure is already assigned then do nothing.
     */
    public void addTableStructure(String tableStructure);

    /**
     * Removes the given table structure from the list of table structures.
     */
    public void removeTableStructure(String tableStructure);

    /**
     * Moves the table structures identified by the indexes up or down by one position. If one of
     * the indexes is 0 (the first object), no object is moved up. If one of the indexes is the
     * number of objects - 1 (the last object) no object is moved down.
     * 
     * @param indexes The indexes identifying the table structures.
     * @param up <code>true</code>, to move the table structures up, <false> to move them down.
     * 
     * @return The new indexes of the moved table structures.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify a table structure.
     */
    public int[] moveTableStructure(int[] indexes, boolean up);

    /**
     * Sets if the table content is mandatory for this table structure usage.
     */
    public void setMandatoryTableContent(boolean mandatoryTableContent);

    /**
     * Returns <code>true</code> if the table content is mandatory for this table structure usage,
     * otherwise <code>false</code>.
     */
    public boolean isMandatoryTableContent();

}
