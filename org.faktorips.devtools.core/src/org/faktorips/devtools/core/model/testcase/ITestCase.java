/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.testcase;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsMetaObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;

/**
 * Specification of a test case.
 * 
 * @author Joerg Ortmann
 */
public interface ITestCase extends IIpsMetaObject {

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
     * Search and return the test case type object in the model.
     * 
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this component is part of.
     * 
     * @return The test case type or <code>null</code> if the test case type can't be found.
     * 
     * @throws CoreException if an error occurs while searching for the test case type.
     */
    public ITestCaseType findTestCaseType(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns all test objects or an empty array if the test case hasn't got any test objects.
     */
    public ITestObject[] getTestObjects();

    /**
     * Returns all policy component objects or an empty array if the test case hasn't got any policy
     * component objects.
     */
    public ITestPolicyCmpt[] getTestPolicyCmpts();

    /**
     * Returns all test value objects or an empty array if the test case hasn't got any test value
     * objects.
     */
    public ITestValue[] getTestValues();

    /**
     * Returns all test rule objects which are related to the given test rule parameter or an empty
     * array if the test case hasn't got such test rule objects.
     */
    public ITestRule[] getTestRule(String testRuleParameter);

    /**
     * Returns all test rule objects or an empty array if the test case hasn't got any test rule
     * objects.
     */
    public ITestRule[] getTestRuleObjects();

    /**
     * Returns all input test objects or an empty array if the test case hasn't got any test input
     * objects.
     */
    public ITestObject[] getInputTestObjects();

    /**
     * Returns all input test value objects or an empty array if the test case hasn't got any test
     * input value objects.
     */
    public ITestValue[] getInputTestValues();

    /**
     * Returns all input policy component objects or an empty array if the test case hasn't got any
     * input policy component objects.
     */
    public ITestPolicyCmpt[] getInputTestPolicyCmpts();

    /**
     * Returns all expected result test objects or an empty array if the test case hasn't got any
     * test expected result objects.
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
     * hasn't got any expected result test policy component objects.
     */
    public ITestPolicyCmpt[] getExpectedResultTestPolicyCmpts();

    /**
     * Returns all test policy components of this test case.<br>
     * Returns an empty array if the test case has no test policy components.
     */
    public ITestPolicyCmpt[] getAllTestPolicyCmpt() throws CoreException;

    /**
     * Returns all test objects of this test case.<br>
     * Returns an empty array if the test case has no test objects components.
     */
    public ITestObject[] getAllTestObjects() throws CoreException;

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
     * @param testPolicyCmptPath the complete path to the policy component within the test case
     *            including links and parent type names.
     * 
     * @throws CoreException in case of an error.
     * 
     */
    public ITestPolicyCmpt findTestPolicyCmpt(String testPolicyCmptPath) throws CoreException;

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
     * 
     * @throws CoreException if an error occurs while sorting the objects.
     */
    public void sortTestObjects() throws CoreException;

    /**
     * Returns all validation rules from the test policy components which are related by the test
     * policy component type parameters inside this test case. And additional all validation rules
     * which are related by the policy cmpt's inside this test case, e.g. if the test case type
     * specifies an abstract policy component type (A) and the test case includes a concrete
     * instance of this abstract policy component type (B) then the validation rules A and B will be
     * returned.
     * 
     * @param ipsProject The IPS project which object path is used to search.
     * 
     * @throws CoreException if an error occurs
     */
    public IValidationRule[] getTestRuleCandidates(IIpsProject ipsProject) throws CoreException;

    /**
     * Searches and returns the validation rule with the given name which is inside the test case.
     * The rule will be searched in all policy component which are related by test test policy
     * cmpt's.
     * 
     * @see #getTestRuleCandidates(IIpsProject)
     */
    public IValidationRule findValidationRule(String validationRuleName, IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the qualified names of all referenced product components. Returns an empty array if
     * no product components are referenced.
     */
    public String[] getReferencedProductCmpts() throws CoreException;

    /**
     * Clears the test values of the given test parameter type (input, expected or combined). The
     * values of all test objects in the test case will be cleared.
     * 
     * @throws CoreException if an error occurs
     */
    public void clearTestValues(TestParameterType testParameterType) throws CoreException;

}
