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
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.testcasetype.ITestRuleParameter;

/**
 * Specification of a test rule.
 * 
 * @author Joerg Ortmann
 */
public interface ITestRule extends ITestObject {

    /** Property names */
    String PROPERTY_TEST_RULE_PARAMETER = "testRuleParameter"; //$NON-NLS-1$

    String PROPERTY_VALIDATIONRULE = "validationRule"; //$NON-NLS-1$

    String PROPERTY_VIOLATED = "violationType"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    String MSGCODE_PREFIX = "TESTRULE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the validation rule is inside the policy components
     * which are related by the test policy component type parameters inside the test case type.
     */
    String MSGCODE_VALIDATION_RULE_NOT_EXISTS = MSGCODE_PREFIX + "ValidationRuleNotExists"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that validation rule is duplicated in the test case.
     */
    String MSGCODE_DUPLICATE_VALIDATION_RULE = MSGCODE_PREFIX + "DuplicateValidationRule"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the corresponding test rule parameter not exists.
     */
    String MSGCODE_TEST_RULE_PARAM_NOT_FOUND = MSGCODE_PREFIX + "TestRuleParamNotFound"; //$NON-NLS-1$

    /**
     * Returns the test rule parameter.
     */
    String getTestRuleParameter();

    /**
     * Sets the test rule parameter.
     */
    void setTestRuleParameter(String testRuleParameter);

    /**
     * Returns the test rule parameter or <code>null</code> if the object does not exist.
     * 
     * @param ipsProject The IPS project which object path is used to search.
     * 
     * @throws IpsException if an error occurs while searching for the test rule parameter.
     */
    ITestRuleParameter findTestRuleParameter(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns the validation rule.
     */
    String getValidationRule();

    /**
     * Sets the validation rule.
     */
    void setValidationRule(String ruleParameter);

    /**
     * Returns the validation rule or <code>null</code> if the object does not exist.
     * 
     * @param ipsProject The IPS project which object path is used to search.
     * 
     * @throws IpsException if an error occurs while searching for the validation rule.
     */
    IValidationRule findValidationRule(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns the violation type.
     * 
     * @see TestRuleViolationType
     * 
     */
    TestRuleViolationType getViolationType();

    /**
     * Sets if the validation rule is expected to be violated or not expected to be violated.
     * 
     * @see ITestRule#getViolationType()
     */
    void setViolationType(TestRuleViolationType violated);

}
