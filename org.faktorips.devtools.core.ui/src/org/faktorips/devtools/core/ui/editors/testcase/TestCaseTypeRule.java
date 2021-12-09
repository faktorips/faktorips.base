/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase;

import java.util.HashMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcase.ITestRule;
import org.faktorips.devtools.model.testcasetype.ITestParameter;
import org.faktorips.devtools.model.testcasetype.ITestRuleParameter;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;
import org.faktorips.util.ArgumentCheck;

/**
 * Class to represent a test rule parameter object inside the ui, e.g. the tree viewer.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTypeRule implements IDummyTestCaseObject {

    private ITestCase testCase;
    private ITestRuleParameter testRuleParameter;

    public TestCaseTypeRule(ITestCase testCase, ITestRuleParameter testRuleParameter) {
        ArgumentCheck.notNull(testCase);
        ArgumentCheck.notNull(testRuleParameter);
        this.testCase = testCase;
        this.testRuleParameter = testRuleParameter;
    }

    /**
     * Returns the test rule parameter.
     */
    public ITestRuleParameter getTestRuleParameter() {
        return testRuleParameter;
    }

    /**
     * Returns the name of the corresponding test rule parameter.
     */
    public String getName() {
        return testRuleParameter.getName();
    }

    /**
     * Returns the image of the corresponding test rule parameter.
     */
    public ImageDescriptor getImageDescriptor() {
        return IpsUIPlugin.getImageHandling().getImageDescriptor(testRuleParameter);
    }

    @Override
    public Severity getValidationResultSeverity(IIpsProject ipsProject) throws CoreRuntimeException {
        return Severity.NONE;
    }

    @Override
    public boolean isValid(IIpsProject ipsProject) throws CoreRuntimeException {
        return false;
    }

    @Override
    public MessageList validate(IIpsProject ipsProject) throws CoreRuntimeException {
        MessageList messageList = new MessageList();
        validate(messageList, ipsProject);
        return messageList;
    }

    /**
     * Validate the test policy component association parameters. And validate the min and max
     * instances of the test policy component type parameter by validating the parent test policy
     * component
     */
    private void validate(MessageList list, IIpsProject ipsProject) throws CoreRuntimeException {
        // delegate the validation to the test rules
        HashMap<String, Message> messages = new HashMap<>();
        ITestRule[] testRules = testCase.getTestRule(testRuleParameter.getName());
        for (ITestRule testRule : testRules) {
            MessageList msgList = testRule.validate(ipsProject);
            for (Message msg : msgList) {
                messages.put(msg.getCode(), msg);
            }
        }

        // add the unique test rule messages to the list of messages
        for (Message message : messages.values()) {
            list.add(message);
        }
    }

    @Override
    public ITestParameter getTestParameter() {
        return getTestRuleParameter();
    }

    @Override
    public IIpsProject getIpsProject() {
        return testCase.getIpsProject();
    }
}
