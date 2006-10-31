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
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;

/**
 * Specification of a test policy component relation.
 * 
 * @author Joerg Ortmann
 */
public interface ITestPolicyCmptRelation extends IIpsObjectPart  {
	
	/** Property names */
	public final static String PROPERTY_POLICYCMPTTYPE = "testPolicyCmptType"; //$NON-NLS-1$
    public final static String PROPERTY_TARGET = "target"; //$NON-NLS-1$
    
    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "TESTPOLICYCMPTRELATION-"; //$NON-NLS-1$

    /**
	 * Validation message code to indicate that the target of an assoziation is not in the test case.
	 */
	public final static String MSGCODE_ASSOZIATION_TARGET_NOT_IN_TEST_CASE = MSGCODE_PREFIX
		+ "AssoziationTargetNotInTestCase"; //$NON-NLS-1$
    
    /**
	 * Validation message code to indicate that the corresponding test case type parameter not exists.
	 */
	public final static String MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND = MSGCODE_PREFIX
		+ "TestCaseTypeParamNotFound"; //$NON-NLS-1$
	
    /**
	 * Validation message code to indicate that the model relation which is related by the corresponding 
	 * test case type parameter not exists.
	 */
	public final static String MSGCODE_MODEL_RELATION_NOT_FOUND = MSGCODE_PREFIX
		+ "ModelRelationNotFound"; //$NON-NLS-1$

    /**
     * Returns the name of test policy component type parameter.
     */
	public String getTestPolicyCmptTypeParameter();
	
    /**
     * Sets the name of test policy component type parameter.
     */	
	public void setTestPolicyCmptTypeParameter(String pcType);
	
    /**
     * Returns the test policy component type parameter or <code>null</code> if the test policy
     * component type parameter doesn't exists.
     * 
     * @throws CoreException if an error occurs while searching for the test policy component type
     *             parameter.
     */	
	public ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter() throws CoreException;
	
	/**
	 * Returns the target.
	 */
	public String getTarget();
	
	/**
	 * Sets the target.
	 */
	public void setTarget(String target);

    /**
     * Returns the target or <code>null</code> if the targte does not exists.
     * 
     * @throws CoreException if an error occurs while searching for the target.
     */		
	public ITestPolicyCmpt findTarget() throws CoreException;
	
    /**
     * Creates a new test policy component as child and returns it.
     */
	public ITestPolicyCmpt newTargetTestPolicyCmptChild();	
	
	/**
	 * Returns <code>true</code> if the relation is an accociation.
	 */
	public boolean isAccoziation();
	
	/**
	 * Returns <code>true</code> if the relation is a composition.
	 */
	public boolean isComposition();
	
	/**
	 * Returns the test case this relation belongs to.
	 */
	public ITestCase getTestCase();	
}
