/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.testcase;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.type.IAttribute;

/**
 * Specification of a test policy component.
 * 
 * @author Joerg Ortmann
 */
public interface ITestPolicyCmpt extends ITestObject {

    public static final String PROPERTY_TESTPOLICYCMPTTYPE = "testPolicyCmptType"; //$NON-NLS-1$
    public static final String PROPERTY_PRODUCTCMPT = "productCmpt"; //$NON-NLS-1$
    public static final String PROPERTY_POLICYCMPTTYPE = "policyCmptType"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "TESTPOLICYCMPT-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the corresponding test case type parameter not
     * exists.
     */
    public static final String MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND = MSGCODE_PREFIX + "TestCaseTypeParamNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a product component is required.
     */
    public static final String MSGCODE_PRODUCT_CMPT_IS_REQUIRED = MSGCODE_PREFIX + "ProductCmptIsRequired"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the product component was not found.
     */
    public static final String MSGCODE_PRODUCT_CMPT_NOT_EXISTS = MSGCODE_PREFIX + "ProductCmptNotExists"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the required product component is set to
     * <code>false</code> but there is a product component specified.
     */
    public static final String MSGCODE_PRODUCT_COMPONENT_NOT_REQUIRED = MSGCODE_PREFIX + "ProductComponentNotRequired"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the minimum instances aren't reached.
     */
    public static final String MSGCODE_MIN_INSTANCES_NOT_REACHED = MSGCODE_PREFIX + "MinInstancesNotReached"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the max instances are reached.
     */
    public static final String MSGCODE_MAX_INSTANCES_REACHED = MSGCODE_PREFIX + "MaxInstancesReached"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the product component is not allowed for the link.
     */
    public static final String MSGCODE_WRONG_PRODUCT_CMPT_OF_LINK = MSGCODE_PREFIX + "WrongProductCmptOfLink"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the product component of the parent of a link is not
     * specified.
     */
    public static final String MSGCODE_PARENT_PRODUCT_CMPT_OF_LINK_NOT_SPECIFIED = MSGCODE_PREFIX
            + "ParentProductCmptOfLinkNotSpecified"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the policy component type not exists.
     */
    public static final String MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS = MSGCODE_PREFIX + "PolicyCmptTypeNotExists"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the policy component type is not equal or not a
     * subtype of the policy component type specified in the test case type parameter.
     */
    public static final String MSGCODE_POLICY_CMPT_TYPE_NOT_ASSIGNABLE = MSGCODE_PREFIX + "PolicyCmptTypeNotAssignable"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the policy component type and a product component
     * are given. If a policy component type is given then no product component is allowed and vice
     * versa.
     * 
     * @see #getPolicyCmptType()
     */
    public static final String MSGCODE_POLICY_CMPT_TYPE_AND_PRODUCT_CMPT_TYPE_GIVEN = MSGCODE_PREFIX
            + "PolicyCmptTypeAndProductCmptTypeGiven"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the policy component type is abstract and could not
     * instantiated during the test run.
     */
    public static final String MSGCODE_POLICY_CMPT_TYPE_IS_ABSTRACT = MSGCODE_PREFIX + "policyCmptTypeIsAbstract"; //$NON-NLS-1$

    /**
     * Returns the qualified name of the test policy component type parameter class.
     */
    public String getTestPolicyCmptTypeParameter();

    /**
     * Sets the name of the test policy component type parameter.
     */
    public void setTestPolicyCmptTypeParameter(String testPolicyCmptTypeParameter);

    /**
     * Returns the test policy component type parameter or <code>null</code> if the test policy
     * component type parameter does not exists.
     * 
     * @throws IpsException if an error occurs while searching for the policy component type.
     */
    public ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns the qualified name of the product component.
     */
    public String getProductCmpt();

    /**
     * Sets the qualified name of the product component.
     */
    public void setProductCmpt(String productCmpt);

    /**
     * Returns the product component or <code>null</code> if the product component does not exists.
     * 
     * @throws IpsException if an error occurs while searching for the product component.
     */
    public IProductCmpt findProductCmpt(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns <code>true</code> if the given test policy component is product relevant, otherwise
     * <code>false</code>
     */
    public boolean isProductRelevant();

    /**
     * Sets the policy component type this test policy component is related to.
     */
    public void setPolicyCmptType(String policyCmptType);

    /**
     * Returns the qualified name of the policy component type, if the test policy component type
     * parameter doesn't requires a product component, otherwise if the test policy component type
     * parameter requires a product component this method returns <code>null</code>.
     */
    public String getPolicyCmptType();

    /**
     * Sets the unique name of the test policy component.
     */
    public void setName(String name);

    /**
     * Creates a new attribute and returns it.
     */
    public ITestAttributeValue newTestAttributeValue();

    /**
     * Returns the type's attributes.
     */
    public ITestAttributeValue[] getTestAttributeValues();

    /**
     * Returns the attribute with the given name. If more than one attribute with the name exist,
     * the first attribute with the name is returned. Returns <code>null</code> if no attribute with
     * the given name exists.
     */
    public ITestAttributeValue getTestAttributeValue(String name);

    /**
     * Creates a new link and returns it.
     */
    public ITestPolicyCmptLink newTestPolicyCmptLink();

    /**
     * Creates a new link on the test policy component and returns it. The given test policy
     * component type parameter specifies the type of the link.
     * 
     * @param typeParam test policy component type parameter for which the new link will be created.
     *            This is the type definition of the test link
     * @param productCmpt name of the product component if the child of the link requires a product
     *            component otherwise empty
     * @param policyCmptType name of the policy component type if the child of the link don't
     *            requires a product component otherwise empty
     * @param targetName name of the target if the new link should be an association otherwise empty
     * 
     * @throws IpsException if an error occurs while adding the new link. If the productCmpt and the
     *             policyCmptType are both given
     */
    public ITestPolicyCmptLink addTestPcTypeLink(ITestPolicyCmptTypeParameter typeParam,
            String productCmpt,
            String policyCmptType,
            String targetName) throws IpsException;

    /**
     * Creates a new link on the test policy component and returns it. The given test policy
     * component type parameter specifies the type of the link.
     * <p>
     * This operation is able to recursively continue to create links where required and possible.
     * The algorithm works as described in {@link #addRequiredLinks(IIpsProject)}.
     * 
     * @param typeParam test policy component type parameter for which the new link will be created.
     *            This is the type definition of the test link
     * @param productCmpt name of the product component if the child of the link requires a product
     *            component otherwise empty
     * @param policyCmptType name of the policy component type if the child of the link don't
     *            requires a product component otherwise empty
     * @param targetName name of the target if the new link should be an association otherwise empty
     * @param recursivelyAddRequired flag indicating whether further links shall be recursively
     *            added where required and possible
     * 
     * @throws IpsException if an error occurs while adding the new link. If the productCmpt and the
     *             policyCmptType are both given.
     */
    public ITestPolicyCmptLink addTestPcTypeLink(ITestPolicyCmptTypeParameter typeParam,
            String productCmpt,
            String policyCmptType,
            String targetName,
            boolean recursivelyAddRequired) throws IpsException;

    /**
     * Recursively adds all required links where possible.
     * <p>
     * The algorithm works as following:
     * <ol>
     * <li>Links can only be created if the {@linkplain IPolicyCmptTypeAssociation policy component
     * type association} associated with the test policy component is a composition AND a product
     * component is assigned to the test policy component
     * <li>The operation will look at the child test parameters. For each child test parameter, if
     * it's minimum instances count is &gt; 0 (meaning the parameter is not optional), and only one
     * {@linkplain IProductCmpt product component} qualifies for the association, a number of
     * {@linkplain ITestPolicyCmptLink test policy component links} that is equal to the minimum
     * cardinality of the child test parameter will be added
     * <li>If more than one product component qualifies for the association, or the minimum
     * instances count = 0, the {@linkplain IProductCmptLink product component links} of the product
     * component assigned to the test policy component are analyzed. For each product component link
     * to the target with minimum cardinality &gt; 0, a number of {@linkplain ITestPolicyCmptLink
     * test policy component links} equal to the minimum cardinality of the product component link
     * will be added
     * </ol>
     * 
     * @param ipsProject project to use as a base for searching the
     *            {@link ITestPolicyCmptTypeParameter} this test policy component is associated with
     * 
     * @throws IllegalStateException if no product component is assigned to this test policy
     *             component
     * @throws IpsException if an error occurs while searching for the
     *             {@link ITestPolicyCmptTypeParameter} or while adding links
     */
    public void addRequiredLinks(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns all test policy component links.
     */
    public ITestPolicyCmptLink[] getTestPolicyCmptLinks();

    /**
     * Returns the test policy component links with the given name which are related to the given
     * test policy component parameter.
     * 
     */
    public ITestPolicyCmptLink[] getTestPolicyCmptLinks(String typeParameterName);

    /**
     * Returns the first link with the indicated test policy component type name or null if no such
     * link exists.
     * <p>
     * Note that a link's name is equal to it's target type, so you can also use the target type as
     * parameter.
     * 
     * @throws IllegalArgumentException if testPolicyCmptType is <code>null</code>.
     */
    public ITestPolicyCmptLink getTestPolicyCmptLink(String testPolicyCmptType);

    /**
     * Returns the test case which this test policy component belongs to. This object could be
     * directly a child of a test case or a child of another test policy component. The top level
     * test case of the test case hierarchy will be returned.
     */
    public ITestCase getTestCase();

    /**
     * Returns <code>true</code> if this object is a root or <code>false</code> if this object is a
     * child object.
     */
    @Override
    public boolean isRoot();

    /**
     * Returns the parent test policy component.
     */
    public ITestPolicyCmpt getParentTestPolicyCmpt();

    /**
     * Removes the given link.
     * 
     * @throws IpsException in case of an error.
     */
    public void removeLink(ITestPolicyCmptLink link) throws IpsException;

    /**
     * Updates the default for all test attribute values. The default will be retrieved from the
     * product component or if no product component is available or the attribute isn't configured
     * by product then from the policy component. Don't update the value if not default is
     * specified.
     * 
     * @throws IpsException in case of an error.
     */
    public void updateDefaultTestAttributeValues() throws IpsException;

    /**
     * Moves the test policy component link identified by the indexes up or down by one position. If
     * one of the indexes is 0 (the first link), nothing is moved up. If one of the indexes is the
     * number of parameters - 1 (the last link) nothing moved down
     * 
     * @param indexes The indexes identifying the test policy component link.
     * @param up <code>true</code>, to move up, <code>false</code> to move them down.
     * 
     * @return The new indexes of the test policy component link.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify a test policy
     *             component link.
     */
    public int[] moveTestPolicyCmptLink(int[] indexes, boolean up);

    /**
     * Returns the index of the given child test policy component. The index starts with 0 (the
     * first element).
     * 
     * @throws IpsException if the given test policy component is no child of the current test
     *             policy component.
     */
    public int getIndexOfChildTestPolicyCmpt(ITestPolicyCmpt testPolicyCmpt) throws IpsException;

    /**
     * Searches the given attribute in the supertype of the product component type which is stored
     * in this test object. Returns <code>null</code> if the attribute doesn't exists on the product
     * cmpt types supertype hierarchy the product component is based on or no product component is
     * set.
     * 
     * @param ipsProject The IPS project which object path is used to search.
     * 
     * @throws IpsException if an error occurs while searching.
     */
    public IAttribute findProductCmptTypeAttribute(String attribute, IIpsProject ipsProject) throws IpsException;

    /**
     * Returns the policy component type this test object is related to, if this object is
     * configured by product then the products policy component type will be returned, otherwise the
     * policy component type will be searched using the stored policy component type name. Return
     * <code>null</code> if the policy component type wasn't found.
     */
    public IPolicyCmptType findPolicyCmptType();

    /**
     * If {@link #getProductCmpt()} would return <code>null</code> or "" (empty string) this method
     * returns <code>false</code>. For all qualified-name values aside from <code>null</code> and ""
     * (empty string) this method returns <code>true</code>, regardless of the existence of a
     * product component with the specified name.
     * 
     * @return <code>true</code> if the qualified name of a product component is specified in this
     *         {@link ITestPolicyCmpt}, <code>false</code> otherwise.
     * @since FIPS 3.0.0
     */
    public boolean hasProductCmpt();

    /**
     * Sets this instance's product component to the given qualified name and modifies this
     * instance's display-name accordingly (if applicable). Renaming {@link ITestPolicyCmpt} will
     * retain the standard-naming or manual naming respectively as well as the uniqueness of the
     * name.
     * <p>
     * The standard naming of a {@link ITestPolicyCmpt} is as follows:
     * <ul>
     * <li>If this TestPolicyComponent has a product component, it is named after it (with possible
     * postfix "(x)" for uniqueness)</li>
     * <li>If this TestPolicyComponent does not have a product component, it is named after its
     * TestPolicyCmptTypeParameter.</li>
     * </ul>
     * In all other cases the name is assumed to be manually modified.
     * <p>
     * This method renames this {@link ITestPolicyCmpt} if it has standard-naming, but does not
     * change it if it was set manually. If the given qualified name is empty (null or "") there are
     * two cases:
     * <ul>
     * <li>if this {@link ITestPolicyCmpt} has a standard name it will be named after its
     * TestPolicyCmptTypeParameter (reset to default)</li>
     * <li>If this {@link ITestPolicyCmpt} has a manual name, it will not be changed at all.</li>
     * </ul>
     * 
     * @since FIPS 3.0.0
     * @param prodCmptQName the qualified name of this test component's new product component.
     */
    public void setProductCmptAndNameAfterIfApplicable(String prodCmptQName);

}
