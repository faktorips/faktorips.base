/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.testcasetype;

import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;

/**
 * Specification of a test policy component parameter.
 * 
 * @author Joerg Ortmann
 */
public interface ITestPolicyCmptTypeParameter extends ITestParameter {

    public static final String PROPERTY_POLICYCMPTTYPE = "policyCmptType"; //$NON-NLS-1$
    public static final String PROPERTY_ASSOCIATION = "association"; //$NON-NLS-1$
    public static final String PROPERTY_REQUIRES_PRODUCTCMT = "requiresProductCmpt"; //$NON-NLS-1$
    public static final String PROPERTY_MIN_INSTANCES = "minInstances"; //$NON-NLS-1$
    public static final String PROPERTY_MAX_INSTANCES = "maxInstances"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "TESTPOLICYCMPTTYPEPARAMETER-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the policy component type was not found.
     */
    public static final String MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS = MSGCODE_PREFIX + "PolicyCmptTypeNotExists"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the minimum instances is less or equal the max
     * instances.
     */
    public static final String MSGCODE_MIN_INSTANCES_IS_GREATER_THAN_MAX = MSGCODE_PREFIX
            + "MinInstancesIsGreaterThanMax"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the max instances is greater or equal the minimum
     * instances.
     */
    public static final String MSGCODE_MAX_INSTANCES_IS_LESS_THAN_MIN = MSGCODE_PREFIX + "MaxInstancesIsLessThanMin"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the type doesn't matches the parent type.<br>
     * E.g. the parent has input type and the attribute has the expected result type.
     */
    public static final String MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE = MSGCODE_PREFIX + "TypeDoesNotMatchParentType"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the association wasn't found.
     */
    public static final String MSGCODE_ASSOCIATION_NOT_EXISTS = MSGCODE_PREFIX + "AssociationNotExists"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the target of the association wasn't found.
     */
    public static final String MSGCODE_TARGET_OF_ASSOCIATION_NOT_EXISTS = MSGCODE_PREFIX
            + "TargetOfAssociationNotExists"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the policy component type is not allowed for the
     * association.
     */
    public static final String MSGCODE_WRONG_POLICY_CMPT_TYPE_OF_ASSOCIATION = MSGCODE_PREFIX
            + "WrongPolicyCmptTypeOfAssociation"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the policy component type must have the required
     * product component flag if the parameter is root and abstract, otherwise the derived class
     * couldn't be determined in the test case.
     */
    public static final String MSGCODE_MUST_REQUIRE_PROD_IF_ROOT_AND_ABSTRACT = MSGCODE_PREFIX
            + "MustRequireProdIfRootAndAbstract"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the policy component type must be configured by
     * product if the requires-product flag is set to <code>true</code>.
     */
    public static final String MSGCODE_REQUIRES_PROD_BUT_POLICY_CMPT_TYPE_IS_NOT_CONF_BY_PROD = MSGCODE_PREFIX
            + "RequiresProdButPolicyCmptTypeIsNotConfByProd"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the target of an association is not in the test case
     * type.
     */
    public static final String MSGCODE_TARGET_OF_ASSOCIATION_NOT_EXISTS_IN_TESTCASETYPE = MSGCODE_PREFIX
            + "TargetOfAssociationNotExistsInTestcasetype"; //$NON-NLS-1$

    /**
     * Returns the qualified name of policy component class.
     */
    public String getPolicyCmptType();

    /**
     * Sets the qualified name of the policy component class.
     */
    public void setPolicyCmptType(String pcType);

    /**
     * Returns the policy component type or <code>null</code> if the policy component type does not
     * exists.
     * 
     * @param ipsProject the IpsProject where to start the search from
     */
    public IPolicyCmptType findPolicyCmptType(IIpsProject ipsProject);

    /**
     * Returns the name of the association.
     */
    public String getAssociation();

    /**
     * Sets the name of the association.
     */
    public void setAssociation(String association);

    /**
     * Returns the association or <code>null</code> if the association does not exists.
     * 
     * @param ipsProject the IpsProject where to start the search from
     * 
     * @throws CoreRuntimeException if an error occurs while searching for the association.
     */
    public IPolicyCmptTypeAssociation findAssociation(IIpsProject ipsProject) throws CoreRuntimeException;

    /**
     * Creates a new input test attribute and returns it.
     * 
     * @throws CoreRuntimeException if the attribute could not be added
     */
    public ITestAttribute newInputTestAttribute() throws CoreRuntimeException;

    /**
     * Creates a new expected result test attribute and returns it.
     * 
     * @throws CoreRuntimeException if the attribute could not be added
     */
    public ITestAttribute newExpectedResultTestAttribute() throws CoreRuntimeException;

    /**
     * Returns the type's attributes.
     */
    public ITestAttribute[] getTestAttributes();

    /**
     * Returns the attribute with the given name. If more than one attribute with the name exist,
     * the first attribute is returned. Returns <code>null</code> if no attribute with the given
     * name exists.
     */
    public ITestAttribute getTestAttribute(String attributeName);

    /**
     * Returns an array of all <code>ITestAttribute</code>s that are based on the
     * <code>IPolicyCmptTypeAttribute</code> with the given name.
     * <p>
     * Returns an empty array if no <code>ITestAttribute</code>s based on the
     * <code>IPolicyCmptTypeAttribute</code> identified by the provided <code>attributeName</code>
     * exist.
     * 
     * @param attributeName The name of the <code>IPolicyCmptTypeAttribute</code> that results must
     *            be based upon.
     */
    public ITestAttribute[] getTestAttributes(String attributeName);

    /**
     * Creates a new child test policy component type parameter and returns it.
     */
    public ITestPolicyCmptTypeParameter newTestPolicyCmptTypeParamChild();

    /**
     * Returns the parent test policy component type parameter or <code>null</code> if this
     * parameter specifies a root parameter.
     */
    public ITestPolicyCmptTypeParameter getParentTestPolicyCmptTypeParam();

    /**
     * Returns the child of the test policy component type parameter.
     */
    public ITestPolicyCmptTypeParameter[] getTestPolicyCmptTypeParamChilds();

    /**
     * Returns the test policy component type parameter child with the given name. If more than one
     * parameter with the name exist, the first object is returned. Returns <code>null</code> if no
     * parameter with the given name exists.
     */
    public ITestPolicyCmptTypeParameter getTestPolicyCmptTypeParamChild(String name);

    /**
     * Returns <code>true</code> if corresponding test policy components have associations to
     * product components instead of policy components.
     */
    public boolean isRequiresProductCmpt();

    /**
     * Sets if the corresponding test policy components should define associations to product
     * components instead of policy components.
     */
    public void setRequiresProductCmpt(boolean requiresProductCmpt);

    /**
     * Returns the minimum instances of the association.
     */
    public int getMinInstances();

    /**
     * Sets the minimum instances of the association.
     */
    public void setMinInstances(int minInstances);

    /**
     * Returns the maximum allowed instances of the association.
     */
    public int getMaxInstances();

    /**
     * Sets the maximum allowed instances of the association.
     */
    public void setMaxInstances(int manInstances);

    /**
     * Removes the given test policy component type parameter from the list of childs.
     */
    public void removeTestPolicyCmptTypeParamChild(ITestPolicyCmptTypeParameter testPolicyCmptTypeParamChildName);

    /**
     * Moves the test attributes identified by the indexes up or down by one position. If one of the
     * indexes is 0 (the first test attribute), no test attribute is moved up. If one of the indexes
     * is the number of attributes - 1 (the last test attribute) no attribute is moved down.
     * 
     * @param indexes The indexes identifying the attributes.
     * @param up <code>true</code>, to move the attributes up, <code>false</code> to move them down.
     * 
     * @return The new indexes of the moved attributes.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify an attribute.
     */
    public int[] moveTestAttributes(int[] indexes, boolean up);

    /**
     * Moves the child identified by the indexes up or down by one position. If one of the indexes
     * is 0 (the first child), nothing is moved up. If one of the indexes is the number of child - 1
     * (the last child) nothing is moved down.
     * 
     * @param indexes The indexes identifying the child.
     * @param up <code>true</code>, to move the child up, <code>false</code> to move them down.
     * 
     * @return The new indexes of the moved child.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify an child.
     */
    public int[] moveTestPolicyCmptTypeChild(int[] indexes, boolean up);

    /**
     * Returns all allowed product components source files which could be added as target of child
     * test policy component type parameters. Only product components are allowed which are defined
     * as targets within the given parentProductCmpt, in at least one generation. If the given
     * product component is <code>null</code> then all product components, which matches the
     * association this parameter is related to, are returned.
     * <p>
     * Returns an empty array if no product components are found as valid product component.
     */
    public IIpsSrcFile[] getAllowedProductCmpt(IIpsProject ipsProjectToSearch, IProductCmpt parentProductCmpt)
            throws CoreRuntimeException;

}
