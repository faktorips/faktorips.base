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

package org.faktorips.devtools.core.ui.editors.testcase;

import java.util.HashMap;
import java.util.Iterator;

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

    /**
     * {@inheritDoc}
     */
    public int getValidationResultSeverity() throws CoreException {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isValid() throws CoreException {
        return false;
    }

    /**
     * {@inheritDoc}
     */
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
        for (int i = 0; i < testRules.length; i++) {
            MessageList msgList = testRules[i].validate(ipsProject);
            for (Iterator<Message> iter = msgList.iterator(); iter.hasNext();) {
                Message msg = iter.next();
                messages.put(msg.getCode(), msg);
            }
        }

        // add the unique test rule messages to the list of messages
        for (Iterator<Message> iter = messages.values().iterator(); iter.hasNext();) {
            list.add(iter.next());
        }
    }

    /**
     * {@inheritDoc}
     */
    public ITestParameter getTestParameter() {
        return getTestRuleParameter();
    }
}
