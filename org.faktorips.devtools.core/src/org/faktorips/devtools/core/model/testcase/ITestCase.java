/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.model.testcase;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;

/**
 * Specification of a test case.
 * 
 * @author Joerg Ortmann
 */
public interface ITestCase extends IIpsObject {

    /** Property names */
    public final static String PROPERTY_TEST_CASE_TYPE = "testCaseType"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "TESTCASE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the corresponding test case type not exists.
     */
    public final static String MSGCODE_TEST_CASE_TYPE_NOT_FOUND = MSGCODE_PREFIX + "TestCaseTypeNotFound"; //$NON-NLS-1$

    /**
     * Returns the test case type name.
     */
    public String getTestCaseType();

    /**
     * Returns the delta between this test case and it's test case type.
     * 
     * @throws CoreException if an error occurs
     */
    public ITestCaseTestCaseTypeDelta computeDeltaToTestCaseType() throws CoreException;
    
    /**
     * Fixes all differences that are described in the delta.   
     */
    public void fixDifferences(ITestCaseTestCaseTypeDelta delta) throws CoreException;
    
    /**
     * Sets the test case type the test case belongs to.
     */
    public void setTestCaseType(String testCaseType);

    /**
     * Search and return the test case type object in the model. Returns <code>null</code> if the
     * test case type not found.
     * 
     * @throws CoreException if an error occurs while searching for the test case type.
     */
    public ITestCaseType findTestCaseType() throws CoreException;

    /**
     * Returns all test objects or an empty array if the test case hasn't got any
     * test objetcs.
     */
    public ITestObject[] getTestObjects();
    
    /**
     * Returns all policy component objects or an empty array if the test case hasn't got any
     * policy component objetcs.
     */
    public ITestPolicyCmpt[] getTestPolicyCmpts();
    
    /**
     * Returns all test value objects or an empty array if the test case hasn't got any test
     * value objects.
     */
    public ITestValue[] getTestValues();
    
    /**
     * Returns all test rule objects wich are related to the given test rule parameter or an empty
     * array if the test case hasn't got such test rule objects.
     */
    public ITestRule[] getTestRule(String testRuleParameter);
    
    /**
     * Returns all test rule objects or an empty
     * array if the test case hasn't got any test rule objects.
     */
    public ITestRule[] getTestRuleObjects();
    
    /**
     * Returns all input test objects or an empty array if the test case hasn't got any test
     * input objects.
     */
    public ITestObject[] getInputTestObjects();
    
    /**
     * Returns all input test value objects or an empty array if the test case hasn't got any test
     * input value objects.
     */
    public ITestValue[] getInputTestValues();

    /**
     * Returns all input policy component objects or an empty array if the test case hasn't got any
     * input policy component objetcs.
     */
    public ITestPolicyCmpt[] getInputTestPolicyCmpts();

    /**
     * Returns all expected result test objects or an empty array if the test case hasn't got any test
     * expected result objects.
     */
    public ITestObject[] getExpectedResultTestObjects();
    
    /**
     * Returns all expected result test value objects or an empty array if the test case hasn't got
     * any expected result test value objects.
     */
    public ITestValue[] getExpectedResultTestValues();

    /**
     * Returns all expected result test rule objects or an empty array if the test case hasn't got
     * any expected result test rule objects.
     */
    public ITestRule[] getExpectedResultTestRules();

    
    /**
     * Returns all expected result test policy component objects or an empty array if the test case
     * hasn't got any expected result test policy component objetcs.
     */
    public ITestPolicyCmpt[] getExpectedResultTestPolicyCmpts();

    /**
     * Creates a new test value object.
     */
    public ITestValue newTestValue();

    /**
     * Creates a new test rule object.
     */
    public ITestRule newTestRule();
    
    /**
     * Creates a new test policy component object.
     */
    public ITestPolicyCmpt newTestPolicyCmpt();

    /**
     * Returns the test policy component or <code>null</code> if not found.
     * 
     * @param testpolicyCmptPath the complete path to the policy component within the test case including
     *            relations and parent type names.
     *            
     * @throws CoreException in case of an error.
     * 
     */
    public ITestPolicyCmpt findTestPolicyCmpt(String testPolicyCmptPath) throws CoreException ;
    
    /**
     * Returns the corresponing test policy component type parameter of the given test policy
     * component or <code>null</code> if not found.
     * 
     * @param testPolicyCmpt the test policy component for which the type parameter will be
     *            returned.
     * 
     * @throws CoreException if an error occurs while searching for the object.
     */
    public ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(ITestPolicyCmpt testPolicyCmpt)
            throws CoreException;
    
    /**
     * Returns the corresponing test policy component type parameter of the given relation or
     * <code>null</code> if not found.
     * 
     * @param relation the test policy component relation for which test relation will be returned.
     * 
     * @throws CoreException if an error occurs while searching for the object.
     */
    public ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(ITestPolicyCmptRelation relation)
            throws CoreException;

    /**
     * Evaluates and returns an unique name (inside this test case) for the given test policy
     * component.
     */
    public String generateUniqueNameForTestPolicyCmpt(ITestPolicyCmpt newTestPolicyCmpt, String name);

    /**
     * Removes the given object from the list of input or expected result objects.
     * 
     * @throws CoreException if an error occurs while removing the object.
     */
    public void removeTestObject(ITestObject testObject) throws CoreException;
    
    /**
     * Sorts the test objects concerning the test case type.

     * @throws CoreException if an error occurs while sorting the objects.
     */
    public void sortTestObjects() throws CoreException;
}
