/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.testcase;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.testcasetype.ITestRuleParameter;

/**
 * Specification of a test rule.
 * 
 * @author Joerg Ortmann
 */
public interface ITestRule extends ITestObject {

    /** Property names */
    public final static String PROPERTY_TEST_RULE_PARAMETER = "testRuleParameter"; //$NON-NLS-1$

    public final static String PROPERTY_VALIDATIONRULE = "validationRule"; //$NON-NLS-1$

    public final static String PROPERTY_VIOLATED = "violationType"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "TESTRULE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the validation rule is inside the policy cmpts which
     * are related by the test policy cmpt type parameters inside the test case type.
     */
    public final static String MSGCODE_VALIDATION_RULE_NOT_EXISTS = MSGCODE_PREFIX + "ValidationRuleNotExists"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that validatio rule is duplicated in the test case.
     */
    public final static String MSGCODE_DUPLICATE_VALIDATION_RULE = MSGCODE_PREFIX + "DuplicateValidationRule"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the corresponding test rule parameter not exists.
     */
    public final static String MSGCODE_TEST_RULE_PARAM_NOT_FOUND = MSGCODE_PREFIX + "TestRuleParamNotFound"; //$NON-NLS-1$

    /**
     * Returns the test rule parameter.
     */
    public String getTestRuleParameter();

    /**
     * Sets the test rule parameter.
     */
    public void setTestRuleParameter(String testRuleParameter);

    /**
     * Returns the test rule parameter or <code>null</code> if the object does not exists.
     * 
     * @param ipsProject The ips project which object path is used to search.
     * 
     * @throws CoreException if an error occurs while searching for the test rule parameter.
     */
    public ITestRuleParameter findTestRuleParameter(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the validation rule.
     */
    public String getValidationRule();

    /**
     * Sets the validation rule.
     */
    public void setValidationRule(String ruleParameter);

    /**
     * Returns the validation rule or <code>null</code> if the object does not exists.
     * 
     * @param ipsProject The ips project which object path is used to search.
     * 
     * @throws CoreException if an error occurs while searching for the validation rule.
     */
    public IValidationRule findValidationRule(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the violation type.
     * 
     * @see TestRuleViolationType
     * 
     */
    public TestRuleViolationType getViolationType();

    /**
     * Sets if the validation rule is expected to be violated or not expected to be violated.
     * 
     * @see ITestRule#getViolationType()
     */
    public void setViolationType(TestRuleViolationType violated);
}
