/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpttype;

import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.type.IProductCmptProperty;

/**
 * Specification of table structure usage object.
 * <p>
 * Specifies a usage of several table structure for a product component type.
 * 
 * @author Jan Ortmann
 */
public interface ITableStructureUsage extends IProductCmptProperty {

    String TAG_NAME = "TableStructureUsage"; //$NON-NLS-1$
    
    String TAG_NAME_TABLE_STRUCTURE = "TableStructure"; //$NON-NLS-1$
    
    String PROPERTY_ROLENAME = "roleName"; //$NON-NLS-1$

    String PROPERTY_TABLESTRUCTURE = "tableStructure"; //$NON-NLS-1$

    String PROPERTY_MANDATORY_TABLE_CONTENT = "mandatoryTableContent"; //$NON-NLS-1$

    String PROPERTY_CHANGING_OVER_TIME = "changingOverTime"; //$NON-NLS-1$

    String MSGCODE_PREFIX = "TableStructureUsage-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the referenced table structure hasn't been found.
     */
    String MSGCODE_TABLE_STRUCTURE_NOT_FOUND = MSGCODE_PREFIX + "TableStructureNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the role name is invalid.
     */
    String MSGCODE_INVALID_ROLE_NAME = MSGCODE_PREFIX + "InvalidRoleName"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the the usage does not reference at least one
     * structure
     */
    String MSGCODE_MUST_REFERENCE_AT_LEAST_1_TABLE_STRUCTURE = MSGCODE_PREFIX
            + "MustReferenceAtLeast1Structure"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the policy component type is not configurable by
     * product.
     */
    String MSGCODE_POLICYCMPTTYPE_IS_NOT_CONFIGURABLE_BY_PRODUCT = MSGCODE_PREFIX
            + "PolicycmpttypeIsNotConfigurableByProduct"; //$NON-NLS-1$

    /**
     * Validation message code that identifies the validation rule that checks if role name is
     * already defined in the super type.
     */
    String MSGCODE_ROLE_NAME_ALREADY_IN_SUPERTYPE = MSGCODE_PREFIX + "RoleNameAlreadyInSupertype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the table structure usage has the same role name as
     * at least one other usage in the supertype hierarchy.
     */
    String MSGCODE_SAME_ROLENAME = MSGCODE_PREFIX + "SameRoleName"; //$NON-NLS-1$

    /**
     * Returns the role name.
     */
    String getRoleName();

    /**
     * Sets the role name.
     */
    void setRoleName(String s);

    /**
     * Returns all table structures this usage belongs to.<br>
     * Returns an empty array if no table structures are related by this usage object.
     */
    String[] getTableStructures();

    /**
     * Returns <code>true</code> if this usage specified the given table structure as one the
     * structures that are allowed to use, otherwise <code>false</code>. Returns <code>false</code>
     * if tabelStructure is <code>null</code>.
     */
    boolean isUsed(String tableStructure);

    /**
     * Adds the given table structure to the list of table structure this usage object
     * specifies.<br>
     * If the table structure is already assigned then do nothing.
     */
    void addTableStructure(String tableStructure);

    /**
     * Removes the given table structure from the list of table structures.
     */
    void removeTableStructure(String tableStructure);

    /**
     * Moves the table structures identified by the indexes up or down by one position. If one of
     * the indexes is 0 (the first object), no object is moved up. If one of the indexes is the
     * number of objects - 1 (the last object) no object is moved down.
     * 
     * @param indexes The indexes identifying the table structures.
     * @param up <code>true</code>, to move the table structures up, <code>false</code> to move them
     *            down.
     * 
     * @return The new indexes of the moved table structures.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify a table structure.
     */
    int[] moveTableStructure(int[] indexes, boolean up);

    /**
     * Sets if the table content is mandatory for this table structure usage.
     */
    void setMandatoryTableContent(boolean mandatoryTableContent);

    /**
     * Returns <code>true</code> if the table content is mandatory for this table structure usage,
     * otherwise <code>false</code>.
     */
    boolean isMandatoryTableContent();

    /**
     * Returns the {@link IProductCmptType} this table structure usage belongs to.
     */
    IProductCmptType getProductCmptType();

    /**
     * Configures this {@link ITableStructureUsage} to change or be constant over time. If
     * <code>true</code> every {@link IProductCmptGeneration} may specify a different value for this
     * attribute. If <code>false</code> the value is the same for all generations.
     * 
     * @param changingOverTime indicates whether or not this attribute should change over time
     */
    void setChangingOverTime(boolean changingOverTime);

}
