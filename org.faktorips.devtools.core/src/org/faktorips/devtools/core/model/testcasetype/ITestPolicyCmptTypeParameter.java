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

package org.faktorips.devtools.core.model.testcasetype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.testcasetype.TestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;

/**
 *  Specification of a test policy component parameter.
 *  
 * @author Joerg Ortmann
 */
public interface ITestPolicyCmptTypeParameter extends ITestParameter {

	/** Property names */
	public final static String PROPERTY_POLICYCMPTTYPE = "policyCmptType"; //$NON-NLS-1$
	public final static String PROPERTY_RELATION = "relation"; //$NON-NLS-1$	
	public final static String PROPERTY_REQUIRES_PRODUCTCMT = "requiresProductCmpt"; //$NON-NLS-1$
    public final static String PROPERTY_MIN_INSTANCES = "minInstances"; //$NON-NLS-1$
    public final static String PROPERTY_MAX_INSTANCES = "maxInstances"; //$NON-NLS-1$
    
    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "TESTPOLICYCMPTTYPEPARAMETER-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the policy component type was not found.
     */
    public final static String MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS = MSGCODE_PREFIX
    + "PolicyCmptTypeNotExists"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the min instances is less or equal the max instances.
     */
    public final static String MSGCODE_MIN_INSTANCES_IS_GREATER_THAN_MAX = MSGCODE_PREFIX
    + "MinInstancesIsGreaterThanMax"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the max instances is greater or equal the min instances.
     */
    public final static String MSGCODE_MAX_INSTANCES_IS_LESS_THAN_MIN = MSGCODE_PREFIX
    + "MaxInstancesIsLessThanMin"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the role doesn't matches the parent role.<br>
     * E.g. the parent defines the input role and the attribute has the expected result role.
     */
    public final static String MSGCODE_ROLE_DOES_NOT_MATCH_PARENT_ROLE = MSGCODE_PREFIX + "RoleDoesNotMatchParentRole"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the relation wasn't found.
     */
    public final static String MSGCODE_RELATION_NOT_EXISTS = MSGCODE_PREFIX + "RelationNotExists"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the target of the relation wasn't found.
     */
    public final static String MSGCODE_TARGET_OF_RELATION_NOT_EXISTS = MSGCODE_PREFIX + "TargetOfRelationNotExists"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the policy cmpt type is not allowed for the relation.
     */
    public final static String MSGCODE_WRONG_POLICY_CMPT_TYPE_OF_RELATION = MSGCODE_PREFIX + "WrongPolicyCmptTypeOfRelation"; //$NON-NLS-1$
    
    /**
     * Returns the qualified name of policy component class.
     */
	public String getPolicyCmptType();
	
    /**
     * Sets the qualified name of the policy component class.
     */	
	public void setPolicyCmptType(String pcType);
	
    /**
     * Returns the policy component type or <code>null</code> if the policy component type does not exists.
     * 
     * @throws CoreException if an error occurs while searching for the target.
     */	
	public IPolicyCmptType findPolicyCmptType() throws CoreException;
	
    /**
     * Returns the name of the relation.
     */
	public String getRelation();
	
    /**
     * Sets the name of the relation.
     */	
	public void setRelation(String relation);
	
    /**
     * Returns the relation or <code>null</code> if the relation does not exists.
     * 
     * @throws CoreException if an error occurs while searching for the relation.
     */	
	public IRelation findRelation() throws CoreException;
		
    /**
     * Creates a new input test attribute and returns it.
     * 
     * @throws CoreException if the attribute could not be added
     */
    public ITestAttribute newInputTestAttribute() throws CoreException;
    
    /**
     * Creates a new expected result test attribute and returns it.
     * 
     * @throws CoreException if the attribute could not be added
     */
    public ITestAttribute newExpectedResultTestAttribute() throws CoreException;

    /**
     * Returns the type's attributes.
     */
    public ITestAttribute[] getTestAttributes();
    
    /**
     * Returns the attribute with the given name. If more than one attribute
     * with the name exist, the first attribute is returned.
     * Returns <code>null</code> if no attribute with the given name exists. 
     */
    public ITestAttribute getTestAttribute(String attributeName);
    
    /**
     * Creates a new child test policy component type parameter and returns it.
     */
    public ITestPolicyCmptTypeParameter newTestPolicyCmptTypeParamChild();

    /**
     * Returns the child of the test policy component type parameter.
     */
    public ITestPolicyCmptTypeParameter[] getTestPolicyCmptTypeParamChilds();
    
    /**
     * Returns the test policy component type param child with the given name. If more than one parameter
     * with the name exist, the first object is returned.
     * Returns <code>null</code> if no parameter with the given name exists. 
     */
    public ITestPolicyCmptTypeParameter getTestPolicyCmptTypeParamChild(String name);
    
	/** 
	 * Returns <code>true</code> if corresponding test policy components have relations to product components 
	 * instead of policy components.
	 */
	public boolean isRequiresProductCmpt();
	
	/** 
	 * Sets if the corresponding test policy components should define relations to product components
	 * instead of policy components.
	 */
	public void setRequiresProductCmpt(boolean requiresProductCmpt);
	
	/**
	* Returns the minmum instances of the relation.   
	*/
	public int getMinInstances();
	   
	/**
	* Sets the minmum instances of the relation.   
	*/
	public void setMinInstances(int minInstances);
	   
	/**
	* Returns the maximum allowed instances of the relation.   
	*/
	public int getMaxInstances();
	   
	/**
	* Sets the maximum allowed instances of the relation.   
	*/
	public void setMaxInstances(int manInstances);
    
    /**
     * Removes the given test policy component type parameter from the list of childs.
     */
    public void removeTestPolicyCmptTypeParamChild(TestPolicyCmptTypeParameter testPolicyCmptTypeParamChildName);

    /**
     * Moves the test attributes identified by the indexes up or down by one position.
     * If one of the indexes is 0 (the first test attribute), no test attribute is moved up. 
     * If one of the indexes is the number of attributes - 1 (the last test attribute)
     * no attribute is moved down. 
     * 
     * @param indexes   The indexes identifying the attributes.
     * @param up        <code>true</code>, to move the attributes up, 
     * <false> to move them down.
     * 
     * @return The new indexes of the moved attributes.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify
     * an attribute.
     */
    public int[] moveTestAttributes(int[] indexes, boolean up);
}
