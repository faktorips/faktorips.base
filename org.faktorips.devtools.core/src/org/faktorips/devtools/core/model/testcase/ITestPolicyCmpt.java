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

package org.faktorips.devtools.core.model.testcase;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;

/**
 *  Specification of a test policy component.
 *  
 * @author Joerg Ortmann
 */
public interface ITestPolicyCmpt extends ITestObject {
	
	/** Property names */
	public final static String PROPERTY_POLICYCMPTTYPE = "testPolicyCmptType"; //$NON-NLS-1$
	public final static String PROPERTY_PRODUCTCMPT = "productCmpt"; //$NON-NLS-1$
	
    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "TESTPOLICYCMPT-"; //$NON-NLS-1$
	
    /**
	 * Validation message code to indicate that the corresponding test case type parameter not exists.
	 */
	public final static String MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND = MSGCODE_PREFIX
		+ "TestCaseTypeParamNotFound"; //$NON-NLS-1$
    
    /**
	 * Validation message code to indicate that a product component is required.
	 */
	public final static String MSGCODE_PRODUCT_CMPT_IS_REQUIRED = MSGCODE_PREFIX
		+ "ProductCmptIsRequired"; //$NON-NLS-1$
	
    /**
     * Validation message code to indicate that the product component was not found.
     */
    public final static String MSGCODE_PRODUCT_CMPT_NOT_EXISTS = MSGCODE_PREFIX
        + "ProductCmptNotExists"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the required product component is set to <code>false</code>
     * but there is a product component specified.
     */
    public final static String MSGCODE_PRODUCT_COMPONENT_NOT_REQUIRED = MSGCODE_PREFIX
        + "ProductComponentNotRequired"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the min instances aren't reached.
     */
    public final static String MSGCODE_MIN_INSTANCES_NOT_REACHED = MSGCODE_PREFIX
        + "MinInstancesNotReached"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the max instances are reached.
     */
    public final static String MSGCODE_MAX_INSTANCES_REACHED = MSGCODE_PREFIX
        + "MaxInstancesReached"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the product cmpt is not allowed for the relation.
     */
    public final static String MSGCODE_WRONG_PRODUCT_CMPT_OF_RELATION = MSGCODE_PREFIX + "WrongProductCmptOfRelation"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the product cmpt of the parent of a relation is not
     * specified.
     */
    public final static String MSGCODE_PARENT_PRODUCT_CMPT_OF_RELATION_NOT_SPECIFIED = MSGCODE_PREFIX
            + "ParentProductCmptOfRelationNotSpecified"; //$NON-NLS-1$
    
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
     * Returns the qualified name of the product componet.
     */
	public String getProductCmpt();
	
    /**
     * Sets the qualified name of the product componet.
     */	
	public void setProductCmpt(String productCmpt);
	
    /**
     * Returns the product component or <code>null</code> if the product component does not exists.
     * 
     * @throws CoreException if an error occurs while searching for the product component.
     */	
	public IProductCmpt findProductCmpt(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns <code>true</code> if the given test policy cmpt is product relevant,
     * otherwise <code>false</code>
     */ 
    public boolean isProductRelevant();

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
     * Returns the attribute with the given name. If more than one attribute
     * with the name exist, the first attribute with the name is returned.
     * Returns <code>null</code> if no attribute with the given name exists. 
     */
    public ITestAttributeValue getTestAttributeValue(String name);
    
    /**
     * Creates a new relation and returns it.
     */
    public ITestPolicyCmptRelation newTestPolicyCmptRelation();
    
    /**
     * Creates a new relation on the test policy component and returns it. The given test policy
     * component type param specifies the type of the relation.
     * 
     * @param typeParam The test policy component type parameter for which the new relation will be
     *            created. This is the type definition of the test relation.
     * @param productCmpt The name of the product component if the child of the relation requires a
     *            product component otherwise empty.
     * @param targetName The name of the target if the new relation should be an assoziation
     *            otherwise empty.
     * 
     * @throws CoreException if an error occurs while adding the new relation.
     */
    public ITestPolicyCmptRelation addTestPcTypeRelation(ITestPolicyCmptTypeParameter typeParam,
            String productCmpt,
            String targetName) throws CoreException;
    
    /**
     * Returns all test policy component relations.
     */
    public ITestPolicyCmptRelation[] getTestPolicyCmptRelations();

    /**
     * Returns the test policy component relations with the given name 
     * which are related to the given test policy component parameter.
     * 
     */
    public ITestPolicyCmptRelation[] getTestPolicyCmptRelations(String typeParameterName);
    
    /**
     * Returns the first relation with the indicated test policy cmpt type name 
     * or null if no such relation exists.
     * <p>
     * Note that a relation's name is equal to it's target type, so you
     * can also use the target type as parameter.
     * 
     * @throws IllegalArgumentException if testPolicyCmptType is <code>null</code>.
     */
    public ITestPolicyCmptRelation getTestPolicyCmptRelation(String testPolicyCmptType);
    
    /**
     * Returns the test case which this test policy component belongs to.
     * This object could be direcly a child of a test case or a child of another
     * test policy component. The top level test case of the test case hierarchy will be returned.
     */
    public ITestCase getTestCase();
    
    /** 
     * Returns <code>true</code> if this object is a root or <code>false</code> 
     * if this object is a child objejct.
     */
    public boolean isRoot();

    /**
     * Returns the parent test policy component.
     */
    public ITestPolicyCmpt getParentPolicyCmpt();
    
    /**
     * Removes the given relation.
     * 
     * @throws CoreException in case of an error.
     */
    public void removeRelation(ITestPolicyCmptRelation relation) throws CoreException;
    
    /**
     * Updates the default for all test attribute values. The default will be retrieved from the
     * product cmpt or if no product cmpt is available or the attribute isn't configurated by product 
     * then from the policy cmpt. Don't update the value if not default is specified.
     * 
     * @throws CoreException in case of an error.
     */
    public void updateDefaultTestAttributeValues() throws CoreException;
    
    /**
     * Moves the test policy cmpt relation identified by the indexes up or down by one position. If one of the
     * indexes is 0 (the first relation), nothing is moved up. If one of the indexes is the
     * number of parameters - 1 (the last relation) nothing moved down
     * 
     * @param indexes The indexes identifying the test policy cmpt relation.
     * @param up <code>true</code>, to move up, <false> to move them down.
     * 
     * @return The new indexes of the test policy cmpt relation.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify a test policy cmpt relation.
     */
    public int[] moveTestPolicyCmptRelations(int[] indexes, boolean up);

    /**
     * Returns the index of the given child test policy cmpt. The index starts with 0 (the first element).
     * 
     * @throws CoreException if the given test policy cmpt is no child of the current test policy cmpt.
     */
    public int getIndexOfChildTestPolicyCmpt(ITestPolicyCmpt testPolicyCmpt) throws CoreException;
    
    /**
     * Searches the given attribute in the supertype of the product cmpt which is stored in this test object.
     * Returns <code>null</code> if the attribute doesn't exitsts on the policy cmpt types supertype hierarchy
     * the product cmpt is based on or no product cmpt is set.
     * 
     * @param ipsProject The ips project which object path is used to search.
     * 
     * @throws CoreException if an error occurs while searching.
     */    
    public IPolicyCmptTypeAttribute findProductCmptAttribute(String attribute, IIpsProject ipsProject) throws CoreException;   
}
