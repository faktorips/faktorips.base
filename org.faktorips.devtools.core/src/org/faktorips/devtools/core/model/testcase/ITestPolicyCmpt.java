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
import org.faktorips.devtools.core.model.product.IProductCmpt;
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
     * Returns the qualified name of the test policy component type parameter class.
     */
	public String getTestPolicyCmptTypeParameter();
	
    /**
     * Sets the qualified name of the test policy component type parameter class.
     */	
	public void setTestPolicyCmptTypeParameter(String pcType);
	
    /**
     * Returns the test policy component type or <code>null</code> if the policy component type does not exists.
     * 
     * @throws CoreException if an error occurs while searching for the policy component type.
     */	
	public ITestPolicyCmptTypeParameter findTestPolicyCmptType() throws CoreException;
	
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
	public IProductCmpt findProductCmpt() throws CoreException;
	
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
    public ITestPolicyCmptRelation getTestPcTypeRelation(String testPolicyCmptType);
    
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
}
