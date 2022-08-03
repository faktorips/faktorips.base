/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.testcasetype;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsMetaClass;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IValidationRule;

/**
 * Specification of a test case type.
 * 
 * @author Jan Ortmann
 */
public interface ITestCaseType extends IIpsMetaClass {

    /**
     * Prefix for all message codes of this class.
     */
    String MSGCODE_PREFIX = "TESTCASETYPE-"; //$NON-NLS-1$

    /**
     * Creates a new test input value parameter.
     */
    ITestValueParameter newInputTestValueParameter();

    /**
     * Creates a new test input policy component type parameter.
     */
    ITestPolicyCmptTypeParameter newInputTestPolicyCmptTypeParameter();

    /**
     * Creates a new test expected result value parameter.
     */
    ITestValueParameter newExpectedResultValueParameter();

    /**
     * Creates a new test expected result policy component type parameter.
     */
    ITestPolicyCmptTypeParameter newExpectedResultPolicyCmptTypeParameter();

    /**
     * Creates a new test expected result rule parameter.
     */
    ITestRuleParameter newExpectedResultRuleParameter();

    /**
     * Creates a new test combined value parameter.
     */
    ITestValueParameter newCombinedValueParameter();

    /**
     * Creates a new test combined policy component type parameter.
     */
    ITestPolicyCmptTypeParameter newCombinedPolicyCmptTypeParameter();

    /**
     * Search and return the test parameter by the given name.
     * <p>
     * Returns <code>null</code> if the test parameter was not found. Returns the first test
     * parameter if more than one test parameter found.
     * 
     * @throws IpsException if an error occurs.
     */
    ITestParameter getTestParameterByName(String testParameterName) throws IpsException;

    /**
     * Returns all test parameters.
     * <p>
     * Returns an empty list if the test case type contains no test parameters.
     */
    ITestParameter[] getTestParameters();

    /**
     * Returns all input parameters or an empty array if the test case type hasn't got any input
     * parameters.
     */
    ITestParameter[] getInputTestParameters();

    /**
     * Returns all test value parameters or an empty array if the test case type hasn't got any test
     * value parameters.
     */
    ITestValueParameter[] getTestValueParameters();

    /**
     * Returns all test rule parameters or an empty array if the test case type hasn't got any test
     * rule parameters.
     */
    ITestRuleParameter[] getTestRuleParameters();

    /**
     * Returns all root test policy component type parameters or an empty array if the test case
     * type hasn't got any test policy component type parameters.
     */
    ITestPolicyCmptTypeParameter[] getTestPolicyCmptTypeParameters();

    /**
     * Returns all input test value parameters or an empty array if the test case type hasn't got
     * any input test value parameters.
     */
    ITestValueParameter[] getInputTestValueParameters();

    /**
     * Returns all input test policy component type parameters or an empty array if the test case
     * type hasn't got any input test policy component type parameters.
     */
    ITestPolicyCmptTypeParameter[] getInputTestPolicyCmptTypeParameters();

    /**
     * Returns the input test value parameter or <code>null</code> if not found. Returns the first
     * test parameter if more than one test parameter found.
     * 
     * @throws IpsException if an error occurs.
     */
    ITestValueParameter getInputTestValueParameter(String inputTestValueParameter) throws IpsException;

    /**
     * Returns the input test policy component type parameter or <code>null</code> if not found.
     * Returns the first test parameter if more than one test parameter found.
     * 
     * @throws IpsException if an error occurs.
     */
    ITestPolicyCmptTypeParameter getInputTestPolicyCmptTypeParameter(String inputTestPolicyCmptTypeParameter)
            throws IpsException;

    /**
     * Returns all expected result parameters or an empty array if the test case type hasn't got any
     * result parameters.
     */
    ITestParameter[] getExpectedResultTestParameters();

    /**
     * Returns all expected result test value parameters or an empty array if the test case type
     * hasn't got any expected result test value parameters.
     */
    ITestValueParameter[] getExpectedResultTestValueParameters();

    /**
     * Returns all expected result test policy component type parameters or an empty array if the
     * test case type hasn't got any expected result test policy component type parameters.
     */
    ITestPolicyCmptTypeParameter[] getExpectedResultTestPolicyCmptTypeParameters();

    /**
     * Returns the expected result test value parameter or <code>null</code> if not found. Returns
     * the first test parameter if more than one test parameter found.
     * 
     * @throws IpsException if an error occurs.
     */
    ITestValueParameter getExpectedResultTestValueParameter(String expResultTestValueParameter)
            throws IpsException;

    /**
     * Returns the expected result test policy component type parameter or <code>null</code> if not
     * found. Returns the first test parameter if more than one test parameter found.
     * 
     * @throws IpsException if an error occurs.
     */
    ITestPolicyCmptTypeParameter getExpectedResultTestPolicyCmptTypeParameter(
            String expResultTestPolicyCmptTypeParameter)
            throws IpsException;

    /**
     * Evaluates and returns an unique name (inside this test case) for the test attribute.
     */
    String generateUniqueNameForTestAttribute(ITestAttribute testAttribute, String name);

    /**
     * Moves the test parameter identified by the indexes up or down by one position. If one of the
     * indexes is 0 (the first test parameter), nothing is moved up. If one of the indexes is the
     * number of parameters - 1 (the last test parameter) nothing moved down
     * 
     * @param indexes The indexes identifying the test parameter.
     * @param up <code>true</code>, to move up, <code>false</code> to move them down.
     * 
     * @return The new indexes of the moved test parameter.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify a test parameter.
     */
    int[] moveTestParameters(int[] indexes, boolean up);

    /**
     * Returns all validation rules from the test policy components which are related by the test
     * policy component type parameters inside this test case type.
     * 
     * @throws IpsException if an error occurs
     */
    IValidationRule[] getTestRuleCandidates(IIpsProject ipsProject) throws IpsException;

    /**
     * Searches and returns the validation rule with the given name which is inside the test case
     * type. The rule will be searched in all policy component types which are related by test test
     * policy component type parameters.
     */
    IValidationRule findValidationRule(String validationRuleName, IIpsProject ipsProject) throws IpsException;

}
