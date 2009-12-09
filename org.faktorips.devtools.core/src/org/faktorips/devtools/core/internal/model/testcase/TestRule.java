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

package org.faktorips.devtools.core.internal.model.testcase;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestRule;
import org.faktorips.devtools.core.model.testcase.TestRuleViolationType;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestRuleParameter;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test rule class. Defines a validation rule for a specific test case.
 * 
 * @author Joerg Ortmann
 */
public class TestRule extends TestObject implements ITestRule {

    final static String TAG_NAME = "RuleObject"; //$NON-NLS-1$

    private String testRuleParameter = ""; //$NON-NLS-1$

    private String validationRule = ""; //$NON-NLS-1$

    private TestRuleViolationType violationType = TestRuleViolationType.VIOLATED;

    public TestRule(IIpsObject parent, int id) {
        super(parent, id);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        testRuleParameter = element.getAttribute(PROPERTY_TEST_RULE_PARAMETER);
        validationRule = element.getAttribute(PROPERTY_VALIDATIONRULE);
        violationType = TestRuleViolationType.getTestRuleViolationType(element.getAttribute(PROPERTY_VIOLATED));
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_TEST_RULE_PARAMETER, testRuleParameter);
        element.setAttribute(PROPERTY_VALIDATIONRULE, validationRule);
        element.setAttribute(PROPERTY_VIOLATED, violationType.getId());
    }

    @Override
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("ValidationRuleDef.gif"); //$NON-NLS-1$
    }

    public String getTestParameterName() {
        return testRuleParameter;
    }

    @Override
    public boolean isRoot() {
        // no childs are supported, the test value parameter is always a root element
        return true;
    }

    @Override
    public ITestObject getRoot() {
        // no childs are supported, the test rule is always a root element
        return this;
    }

    public String getTestRuleParameter() {
        return testRuleParameter;
    }

    public void setTestRuleParameter(String testRuleParameter) {
        String oldTestRuleParameter = this.testRuleParameter;
        this.testRuleParameter = testRuleParameter;
        valueChanged(oldTestRuleParameter, testRuleParameter);
    }

    public ITestParameter findTestParameter(IIpsProject ipsProject) throws CoreException {
        return findTestRuleParameter(ipsProject);
    }

    public ITestRuleParameter findTestRuleParameter(IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(testRuleParameter)) {
            return null;
        }

        ITestCaseType testCaseType = ((ITestCase)getParent()).findTestCaseType(ipsProject);
        if (testCaseType == null) {
            return null;
        }
        ITestParameter param = testCaseType.getTestParameterByName(testRuleParameter);
        if (param instanceof ITestRuleParameter) {
            return (ITestRuleParameter)param;
        }
        return null;
    }

    public String getValidationRule() {
        return validationRule;
    }

    public void setValidationRule(String validationRule) {
        String oldValidationRule = this.validationRule;
        this.validationRule = validationRule;
        valueChanged(oldValidationRule, validationRule);
    }

    public IValidationRule findValidationRule(IIpsProject ipsProject) throws CoreException {
        ITestCase testCase = (ITestCase)getParent();
        return testCase.findValidationRule(validationRule, ipsProject);
    }

    public TestRuleViolationType getViolationType() {
        return violationType;
    }

    public void setViolationType(TestRuleViolationType violationType) {
        TestRuleViolationType oldViolationType = this.violationType;
        this.violationType = violationType;
        valueChanged(oldViolationType, violationType);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

        // check if the validation rule is inside the test case type structure (one rule of the test
        // policy cmpt type parameter)
        if (findValidationRule(ipsProject) == null) {
            String text = NLS.bind(Messages.TestRule_ValidationError_ValidationRuleNotAvailable, validationRule);
            Message msg = new Message(MSGCODE_VALIDATION_RULE_NOT_EXISTS, text, Message.ERROR, this,
                    PROPERTY_VALIDATIONRULE);
            list.add(msg);
        }

        // check if the validation rule is unique in this test case
        ITestCase testCase = (ITestCase)getParent();
        ITestRule[] rules = testCase.getTestRuleObjects();
        for (int i = 0; i < rules.length; i++) {
            if (rules[i] != this && rules[i].getValidationRule().equals(validationRule)) {
                String text = NLS.bind(Messages.TestRule_ValidationError_DuplicateValidationRule, validationRule);
                Message msg = new Message(MSGCODE_DUPLICATE_VALIDATION_RULE, text, Message.ERROR, this,
                        PROPERTY_VALIDATIONRULE);
                list.add(msg);
                break;
            }
        }

        // check if the test rule parameter exists
        ITestParameter param = findTestRuleParameter(ipsProject);
        if (param == null) {
            String text = NLS.bind(Messages.TestRule_ValidationError_TestRuleParameterNotFound, getTestRuleParameter());
            Message msg = new Message(MSGCODE_TEST_RULE_PARAM_NOT_FOUND, text, Message.ERROR, this,
                    PROPERTY_TEST_RULE_PARAMETER);
            list.add(msg);
        }
    }

    @Override
    public IIpsElement[] getChildren() {
        return new IIpsElement[0];
    }

    @Override
    protected void reinitPartCollections() {

    }

    @Override
    protected void addPart(IIpsObjectPart part) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void removePart(IIpsObjectPart part) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected IIpsObjectPart newPart(Element xmlTag, int id) {
        return null;
    }

    @Override
    public String getName() {
        return getTestRuleParameter() + "/" + getValidationRule(); //$NON-NLS-1$
    }

    public RenameRefactoring getRenameRefactoring() {
        return null;
    }

    public boolean isRenameRefactoringSupported() {
        return false;
    }

}
