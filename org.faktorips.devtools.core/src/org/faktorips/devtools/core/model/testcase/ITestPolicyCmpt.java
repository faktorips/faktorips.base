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
import org.faktorips.devtools.core.ui.editors.testcase.TestCaseTypeRelation;

/**
 *  Specification of a test policy component.
 *  
 * @author Joerg Ortmann
 */
public interface ITestPolicyCmpt extends ITestObject {
	
	/** Property names */
	public final static String PROPERTY_POLICYCMPTTYPE = "testPolicyCmptType"; //$NON-NLS-1$
	public final static String PROPERTY_PRODUCTCMPT = "productCmpt"; //$NON-NLS-1$
	public final static String PROPERTY_LABEL = "label"; //$NON-NLS-1$
	
    /**
     * Returns the qualified name of policy component class.
     */
	public String getTestPolicyCmptType();
	
    /**
     * Sets the qualified name of the policy component class.
     */	
	public void setTestPolicyCmptType(String pcType);
	
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
     * Returns the unique label of the test policy component.
     */
	public String getLabel();
	
    /**
     * Sets the unique label of the test policy component.
     */	
	public void setLabel(String label);	
	
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
    public ITestPolicyCmptRelation newTestPcTypeRelation();

    /**
     * Creates a new transient relation and returns it. The relation will just be created not added to the internal model!.
     */
    public ITestPolicyCmptRelation newTransientTestPcTypeRelation();
    
    /**
     * Creates a new relation and returns it.
     * FIXME
     * @param testPcTypeRelationBase the relation which will be used as base of the new relation
     * @param posNext if <code>true</code> the new object will be added at the next position below the given test policy component
     *  			  <code>false</code> the new object will be added at the end
     * @param productCmpt contains the name of the product component if the child of the relation requires a product component
     * 
     * @throws CoreException if an error occurs while adding the new relation.
     */
    public ITestPolicyCmptRelation addTestPcTypeRelation(TestCaseTypeRelation typeParam, String productCmpt, String targetName) throws CoreException ;
    
    /**
     * Returns the type's relations.
     */
    public ITestPolicyCmptRelation[] getTestPcTypeRelations();

    /**
     * Returns the type's relations which are related to the given test policy component parameter.
     */
    public ITestPolicyCmptRelation[] getTestPcTypeRelations(String typeParameterName);
    
    /**
     * Returns the first relation with the indicated test policy cmpt type name 
     * or null if no such relation exists.
     * <p>
     * Note that a relation's name is equal to it's target role, so you
     * can also use the target role as parameter.
     * 
     * @throws IllegalArgumentException if testPolicyCmptType is <code>null</code>.
     */
    public ITestPolicyCmptRelation getTestPcTypeRelation(String testPolicyCmptType);
    
    /** 
     * Returns <code>true</code> if this object is a root or <code>false</code> 
     * if this object is a child objejct.
     */
    public boolean isRoot();
    
    /**
     * Returns the test case which this test policy component belongs to.
     * This object could be direcly a child of a test case or a child of another
     * test policy component. The top level test case of the test case hierarchy will be returned.
     */
    public ITestCase getTestCase();
    
	/**
	 * Returns the root test policy component element.
	 */    
    public ITestPolicyCmpt getRoot();
    
    /**
     * Returns the parent test policy component.
     */
    public ITestPolicyCmpt getParentPolicyCmpt();
    
    /**
     * Removes the given relation.
     */
    public void removeRelation(ITestPolicyCmptRelation relation); 
}
