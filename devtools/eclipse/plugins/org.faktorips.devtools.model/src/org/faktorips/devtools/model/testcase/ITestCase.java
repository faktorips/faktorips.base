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
import org.faktorips.devtools.model.IIpsMetaObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.TestParameterType;

/**
 * Specification of a test case.
 * 
 * @author Joerg Ortmann
 */
public interface ITestCase extends IIpsMetaObject {

    /** Property names */
    String PROPERTY_TEST_CASE_TYPE = "testCaseType"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    String MSGCODE_PREFIX = "TESTCASE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the corresponding test case type not exists.
     */
    String MSGCODE_TEST_CASE_TYPE_NOT_FOUND = MSGCODE_PREFIX + "TestCaseTypeNotFound"; //$NON-NLS-1$

    /**
     * Returns the test case type name.
     */
    String getTestCaseType();

    @Override
    ITestCaseTestCaseTypeDelta computeDeltaToModel(IIpsProject ipsProject) throws IpsException;

    /**
     * Fixes all differences that are described in the delta.
     */
    void fixDifferences(ITestCaseTestCaseTypeDelta delta) throws IpsException;

    /**
     * Sets the test case type the test case belongs to.
     */
    void setTestCaseType(String testCaseType);

    /**
     * Search and return the test case type object in the model.
     * 
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this component is part of.
     * 
     * @return The test case type or <code>null</code> if the test case type can't be found.
     * 
     * @throws IpsException if an error occurs while searching for the test case type.
     */
    ITestCaseType findTestCaseType(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns all test objects or an empty array if the test case hasn't got any test objects.
     */
    ITestObject[] getTestObjects();

    /**
     * Returns all policy component objects or an empty array if the test case hasn't got any policy
     * component objects.
     */
    ITestPolicyCmpt[] getTestPolicyCmpts();

    /**
     * Returns all test value objects or an empty array if the test case hasn't got any test value
     * objects.
     */
    ITestValue[] getTestValues();

    /**
     * Returns all test rule objects which are related to the given test rule parameter or an empty
     * array if the test case hasn't got such test rule objects.
     */
    ITestRule[] getTestRule(String testRuleParameter);

    /**
     * Returns all test rule objects or an empty array if the test case hasn't got any test rule
     * objects.
     */
    ITestRule[] getTestRuleObjects();

    /**
     * Returns all input test objects or an empty array if the test case hasn't got any test input
     * objects.
     */
    ITestObject[] getInputTestObjects();

    /**
     * Returns all input test value objects or an empty array if the test case hasn't got any test
     * input value objects.
     */
    ITestValue[] getInputTestValues();

    /**
     * Returns all input policy component objects or an empty array if the test case hasn't got any
     * input policy component objects.
     */
    ITestPolicyCmpt[] getInputTestPolicyCmpts();

    /**
     * Returns all expected result test objects or an empty array if the test case hasn't got any
     * test expected result objects.
     */
    ITestObject[] getExpectedResultTestObjects();

    /**
     * Returns all expected result test value objects or an empty array if the test case hasn't got
     * any expected result test value objects.
     */
    ITestValue[] getExpectedResultTestValues();

    /**
     * Returns all expected result test rule objects or an empty array if the test case hasn't got
     * any expected result test rule objects.
     */
    ITestRule[] getExpectedResultTestRules();

    /**
     * Returns all expected result test policy component objects or an empty array if the test case
     * hasn't got any expected result test policy component objects.
     */
    ITestPolicyCmpt[] getExpectedResultTestPolicyCmpts();

    /**
     * Returns all test policy components of this test case.<br>
     * Returns an empty array if the test case has no test policy components.
     */
    ITestPolicyCmpt[] getAllTestPolicyCmpt() throws IpsException;

    /**
     * Returns all test objects of this test case.<br>
     * Returns an empty array if the test case has no test objects components.
     */
    ITestObject[] getAllTestObjects() throws IpsException;

    /**
     * Creates a new test value object.
     */
    ITestValue newTestValue();

    /**
     * Creates a new test rule object.
     */
    ITestRule newTestRule();

    /**
     * Creates a new test policy component object.
     */
    ITestPolicyCmpt newTestPolicyCmpt();

    /**
     * Returns the test policy component or <code>null</code> if not found.
     * 
     * @param testPolicyCmptPath the complete path to the policy component within the test case
     *            including links and parent type names.
     * 
     */
    ITestPolicyCmpt findTestPolicyCmpt(String testPolicyCmptPath);

    /**
     * Evaluates and returns an unique name (inside this test case) for the given test policy
     * component.
     */
    String generateUniqueNameForTestPolicyCmpt(ITestPolicyCmpt newTestPolicyCmpt, String name);

    /**
     * Removes the given object from the list of input or expected result objects.
     * 
     * @throws IpsException if an error occurs while removing the object.
     */
    void removeTestObject(ITestObject testObject) throws IpsException;

    /**
     * Sorts the test objects concerning the test case type.
     * 
     * @throws IpsException if an error occurs while sorting the objects.
     */
    void sortTestObjects() throws IpsException;

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
     * @throws IpsException if an error occurs
     */
    IValidationRule[] getTestRuleCandidates(IIpsProject ipsProject) throws IpsException;

    /**
     * Searches and returns the validation rule with the given name which is inside the test case.
     * The rule will be searched in all policy component which are related by test test policy
     * cmpt's.
     * 
     * @see #getTestRuleCandidates(IIpsProject)
     */
    IValidationRule findValidationRule(String validationRuleName, IIpsProject ipsProject) throws IpsException;

    /**
     * Returns the qualified names of all referenced product components. Returns an empty array if
     * no product components are referenced.
     */
    String[] getReferencedProductCmpts() throws IpsException;

    /**
     * Clears the test values of the given test parameter type (input, expected or combined). The
     * values of all test objects in the test case will be cleared.
     * 
     * @throws IpsException if an error occurs
     */
    void clearTestValues(TestParameterType testParameterType) throws IpsException;

}
