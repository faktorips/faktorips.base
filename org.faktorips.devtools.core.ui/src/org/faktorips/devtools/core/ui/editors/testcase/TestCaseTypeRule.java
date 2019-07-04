/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase;

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestRule;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestRuleParameter;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

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
    public int getValidationResultSeverity() throws CoreException {
        return 0;
    }

    @Override
    public boolean isValid() throws CoreException {
        return false;
    }

    @Override
    public int getValidationResultSeverity(IIpsProject ipsProject) throws CoreException {
        return 0;
    }

    @Override
    public boolean isValid(IIpsProject ipsProject) throws CoreException {
        return false;
    }

    @Override
    public MessageList validate(IIpsProject ipsProject) throws CoreException {
        MessageList messageList = new MessageList();
        validate(messageList, ipsProject);
        return messageList;
    }

    /**
     * Validate the test policy component association parameters. And validate the min and max
     * instances of the test policy component type parameter by validating the parent test policy
     * component
     */
    private void validate(MessageList list, IIpsProject ipsProject) throws CoreException {
        // delegate the validation to the test rules
        HashMap<String, Message> messages = new HashMap<String, Message>();
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
