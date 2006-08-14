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
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;

/**
 *  Specification of a test case.
 *   
 * @author Joerg Ortmann
 */
public interface ITestCase extends IIpsObject  {
	
	/** Property names */
    public final static String PROPERTY_TEST_CASE_TYPE = "testCaseType"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "TESTCASE-"; //$NON-NLS-1$

    /**
	 * Validation message code to indicate that the corresponding test case type not exists.
	 */
	public final static String MSGCODE_TEST_CASE_TYPE_NOT_FOUND = MSGCODE_PREFIX
		+ "TestCaseTypeNotFound"; //$NON-NLS-1$
    
    /**
     * Returns the test case type name.
     */
    public String getTestCaseType();
    
    /**
     * Sets the test case type the test case belongs to.
     */
    public void setTestCaseType(String testCaseType);
    
    /**
     * Search and return the test case type object in the model.
     * Returns <code>null</code> if the test case type not found.
     * 
     * @throws CoreException if an error occurs while searching for the test case type.
     */
    public ITestCaseType findTestCaseType() throws CoreException ;
    
	/**
	 * Creates a new test input value object.
	 */
	public ITestValue newInputValue();

	/**
	 * Creates a new test input policy component object.
	 */
	public ITestPolicyCmpt newInputPolicyCmpt();
	
	/**
	 * Creates a new test expected result value object.
	 */
	public ITestValue newExpectedResultValue();

	/**
	 * Creates a new test expected result policy component object.
	 */
	public ITestPolicyCmpt newExpectedResultPolicyCmpt();
	
	/**
	 * Returns all input objects or an empty array  if the test case hasn't got
	 * any input parameters.
	 */
	public ITestObject[] getInputObjects();
	
	/**
	 * Returns all input value objects or an empty array if the test case hasn't got
	 * any input value objects.
	 */
	public ITestValue[] getInputValues();
	
	/**
	 * Returns all input policy component objects or an empty array if the test case hasn't got
	 * any input policy component objetcs.
	 */
	public ITestPolicyCmpt[] getInputPolicyCmpt();
	
	/**
	 * Returns the input policy component or <code>null</code> if not found.
	 * 
	 * @param typeNamePath the complete path to the policy component within the test case including relations and parent type names.
	 *                     
	 */
	public ITestPolicyCmpt findInputPolicyCmpt(String typeNamePath);
	
	/**
	 * Returns all expected result parametes or an empty array if the test case hasn't got
	 * any expected result objects.
	 */
	public ITestObject[] getExpectedResultObjects();
	
	/**
	 * Returns all expected result value objects or an empty array if the test case hasn't got
	 * any expected result value objects.
	 */
	public ITestValue[] getExpectedResultValues();
	
	/**
	 * Returns all expected result policy component objects or an empty array if the test case hasn't got
	 * any expected result policy component objetcs.
	 */
	public ITestPolicyCmpt[] getExpectedResultPolicyCmpt();	
	
	/**
	 * Returns the expected result test policy component or <code>null</code> if not found.
	 * 
	 * @param typeNamePath the complete path to the policy component within the test case including relations and parent type names.
	 */
	public ITestPolicyCmpt findExpectedResultPolicyCmpt(String typeNamePath);
	
	/**
	 * Removes the given object from the list of input or expected result objects.
	 * 
	 *  @throws CoreException if an error occurs while removing the object.
	 */
	public void removeTestObject(ITestObject testObject) throws CoreException;
	
	/**
	 * Returns the corresponing test policy component type parameter of the given test policy component or
	 * <code>null</code> if not found.
	 *
	 * @param testPolicyCmptBase the test policy component for which the type parameter will be returned.
	 *	
	 * @throws CoreException if an error occurs while searching for the object.
	 */
	public ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(ITestPolicyCmpt testPolicyCmptBase) throws CoreException;

	/**
	 * Returns the corresponing test policy component type parameter of the given relation or
	 * <code>null</code> if not found.
	 * 
	 * @param relation the test policy component relation for which test relation will be returned.
	 * 
	 * @throws CoreException if an error occurs while searching for the object.
	 */
	public ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(ITestPolicyCmptRelation relation) throws CoreException;

	/**
	 * Evaluates and returns an unique label (inside this test case) for the given test policy component.
	 */
	public String generateUniqueLabelForTestPolicyCmpt(ITestPolicyCmpt newTestPolicyCmpt, String label);
}
