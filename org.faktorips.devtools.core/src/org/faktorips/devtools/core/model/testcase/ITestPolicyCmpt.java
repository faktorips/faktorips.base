/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.testcase;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.type.IAttribute;

/**
 * Specification of a test policy component.
 * 
 * @author Joerg Ortmann
 */
public interface ITestPolicyCmpt extends ITestObject {

    /** Property names */
    public final static String PROPERTY_TESTPOLICYCMPTTYPE = "testPolicyCmptType"; //$NON-NLS-1$
    public final static String PROPERTY_PRODUCTCMPT = "productCmpt"; //$NON-NLS-1$
    public final static String PROPERTY_POLICYCMPTTYPE = "policyCmptType"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "TESTPOLICYCMPT-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the corresponding test case type parameter not
     * exists.
     */
    public final static String MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND = MSGCODE_PREFIX + "TestCaseTypeParamNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a product component is required.
     */
    public final static String MSGCODE_PRODUCT_CMPT_IS_REQUIRED = MSGCODE_PREFIX + "ProductCmptIsRequired"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the product component was not found.
     */
    public final static String MSGCODE_PRODUCT_CMPT_NOT_EXISTS = MSGCODE_PREFIX + "ProductCmptNotExists"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the required product component is set to
     * <code>false</code> but there is a product component specified.
     */
    public final static String MSGCODE_PRODUCT_COMPONENT_NOT_REQUIRED = MSGCODE_PREFIX + "ProductComponentNotRequired"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the min instances aren't reached.
     */
    public final static String MSGCODE_MIN_INSTANCES_NOT_REACHED = MSGCODE_PREFIX + "MinInstancesNotReached"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the max instances are reached.
     */
    public final static String MSGCODE_MAX_INSTANCES_REACHED = MSGCODE_PREFIX + "MaxInstancesReached"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the product cmpt is not allowed for the link.
     */
    public final static String MSGCODE_WRONG_PRODUCT_CMPT_OF_LINK = MSGCODE_PREFIX + "WrongProductCmptOfLink"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the product cmpt of the parent of a link is not
     * specified.
     */
    public final static String MSGCODE_PARENT_PRODUCT_CMPT_OF_LINK_NOT_SPECIFIED = MSGCODE_PREFIX
            + "ParentProductCmptOfLinkNotSpecified"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the policy cmpt type not exists.
     */
    public final static String MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS = MSGCODE_PREFIX + "PolicyCmptTypeNotExists"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the policy cmpt type is not equal or not a subtype
     * of the poliy cmpt type specified in the test case type parameter.
     */
    public final static String MSGCODE_POLICY_CMPT_TYPE_NOT_ASSIGNABLE = MSGCODE_PREFIX + "PolicyCmptTypeNotAssignable"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the policy cmpt type and a product cmpt are given.
     * If a policy cmpt type is given then no product cmpt is allowed and vice versa.
     * 
     * @see this#getPolicyCmptType()
     */
    public final static String MSGCODE_POLICY_CMPT_TYPE_AND_PRODUCT_CMPT_TYPE_GIVEN = MSGCODE_PREFIX
            + "PolicyCmptTypeAndProductCmptTypeGiven"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the policy cmpt type is abstract and could not
     * instantiated during the test run.
     */
    public final static String MSGCODE_POLICY_CMPT_TYPE_IS_ABSTRACT = MSGCODE_PREFIX + "policyCmptTypeIsAbstract"; //$NON-NLS-1$

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
     * @throws CoreException if an error occurs while searching for the policy component type.
     */
    public ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(IIpsProject ipsProject) throws CoreException;

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
     * @throws CoreException if an error occurs while searching for the product component.
     */
    public IProductCmpt findProductCmpt(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns <code>true</code> if the given test policy cmpt is product relevant, otherwise
     * <code>false</code>
     */
    public boolean isProductRelevant();

    /**
     * Sets the policy cmpt type this test policy cmpt is related to.
     */
    public void setPolicyCmptType(String policyCmptType);

    /**
     * Returns the qualified name of the policy cmpt type, if the test policy cmpt type parameter
     * doesn't requires a product component, otherwise if the test policy cmpt type parameter
     * requires a product component this method returns <code>null</code>.
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
     * component type param specifies the type of the link.
     * 
     * @param typeParam The test policy component type parameter for which the new link will be
     *            created. This is the type definition of the test link.
     * @param productCmpt The name of the product component if the child of the link requires a
     *            product component otherwise empty.
     * @param policyCmptType The name of the policy component type if the child of the link don't
     *            requires a product component otherwise empty.
     * @param targetName The name of the target if the new link should be an assoziation otherwise
     *            empty.
     * 
     * @throws CoreException if an error occurs while adding the new link. If the productCmpt and
     *             the policyCmptType are both given.
     */
    public ITestPolicyCmptLink addTestPcTypeLink(ITestPolicyCmptTypeParameter typeParam,
            String productCmpt,
            String policyCmptType,
            String targetName) throws CoreException;

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
     * Returns the first link with the indicated test policy cmpt type name or null if no such link
     * exists.
     * <p>
     * Note that a link's name is equal to it's target type, so you can also use the target type as
     * parameter.
     * 
     * @throws IllegalArgumentException if testPolicyCmptType is <code>null</code>.
     */
    public ITestPolicyCmptLink getTestPolicyCmptLink(String testPolicyCmptType);

    /**
     * Returns the test case which this test policy component belongs to. This object could be
     * direcly a child of a test case or a child of another test policy component. The top level
     * test case of the test case hierarchy will be returned.
     */
    public ITestCase getTestCase();

    /**
     * Returns <code>true</code> if this object is a root or <code>false</code> if this object is a
     * child objejct.
     */
    public boolean isRoot();

    /**
     * Returns the parent test policy component.
     */
    public ITestPolicyCmpt getParentTestPolicyCmpt();

    /**
     * Removes the given link.
     * 
     * @throws CoreException in case of an error.
     */
    public void removeLink(ITestPolicyCmptLink link) throws CoreException;

    /**
     * Updates the default for all test attribute values. The default will be retrieved from the
     * product cmpt or if no product cmpt is available or the attribute isn't configurated by
     * product then from the policy cmpt. Don't update the value if not default is specified.
     * 
     * @throws CoreException in case of an error.
     */
    public void updateDefaultTestAttributeValues() throws CoreException;

    /**
     * Moves the test policy cmpt link identified by the indexes up or down by one position. If one
     * of the indexes is 0 (the first link), nothing is moved up. If one of the indexes is the
     * number of parameters - 1 (the last link) nothing moved down
     * 
     * @param indexes The indexes identifying the test policy cmpt link.
     * @param up <code>true</code>, to move up, <false> to move them down.
     * 
     * @return The new indexes of the test policy cmpt link.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify a test policy cmpt
     *             link.
     */
    public int[] moveTestPolicyCmptLink(int[] indexes, boolean up);

    /**
     * Returns the index of the given child test policy cmpt. The index starts with 0 (the first
     * element).
     * 
     * @throws CoreException if the given test policy cmpt is no child of the current test policy
     *             cmpt.
     */
    public int getIndexOfChildTestPolicyCmpt(ITestPolicyCmpt testPolicyCmpt) throws CoreException;

    /**
     * Searches the given attribute in the supertype of the product cmpt type which is stored in
     * this test object. Returns <code>null</code> if the attribute doesn't exists on the product
     * cmpt types supertype hierarchy the product cmpt is based on or no product cmpt is set.
     * 
     * @param ipsProject The ips project which object path is used to search.
     * 
     * @throws CoreException if an error occurs while searching.
     */
    public IAttribute findProductCmptTypeAttribute(String attribute, IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the policy cmpt type this test object is related to, if this object is configurated
     * by product then the products policy cmpt type will be returned, otherwise the policy cmpt
     * type will be searched using the stored policy cmpt type name. Return <code>null</code> if the
     * policy cmpt type wasn't found.
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
     * <p/>
     * The standard naming of a {@link ITestPolicyCmpt} is as follows:
     * <ul>
     * <li>If this TestPolicyComponent has a product component, it is named after it (with possible
     * postfix "(x)" for uniqueness)</li>
     * <li>If this TestPolicyComponent does not have a product component, it is named after its
     * TestPolicyCmptTypeParameter.</li>
     * </ul>
     * In all other cases the name is assumed to be manually modified.
     * <p/>
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
