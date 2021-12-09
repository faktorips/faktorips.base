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

import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.model.testcasetype.ITestParameter;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;

/**
 * Helper class to represent a dummy association depending on the test case type association (test
 * policy component type parameter) and a concrete test policy component which will be the parent of
 * all concrete associations inside the test case. The concrete associations are based on the test
 * case type association.<br>
 * The parent test policy component could be used to add new associations to this test policy
 * component inside the test case (based on the test case type association).<br>
 * Example: this class R contains the association type RT and the test policy component A1. By using
 * this class a new test association RT1 of type RT could be added to the policy component A.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTypeAssociation implements IDummyTestCaseObject {

    /** Contains the type definition of the association */
    private ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter;

    /** Contains the parent inside the test case model of the test association type parameter */
    private ITestPolicyCmpt parentTestPolicyCmpt;

    /**
     * Constructor for testing purposes.
     */
    protected TestCaseTypeAssociation() {
        // Constructor for testing purposes
    }

    public TestCaseTypeAssociation(ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter,
            ITestPolicyCmpt parentTestPolicyCmpt) {

        this.testPolicyCmptTypeParameter = testPolicyCmptTypeParameter;
        this.parentTestPolicyCmpt = parentTestPolicyCmpt;
    }

    /**
     * Returns the test association type parameter.
     */
    public ITestPolicyCmptTypeParameter getTestPolicyCmptTypeParam() {
        return testPolicyCmptTypeParameter;
    }

    /**
     * Returns the test policy component (concrete instance inside the test case model) which is the
     * parent of the test association type.
     */
    public ITestPolicyCmpt getParentTestPolicyCmpt() {
        return parentTestPolicyCmpt;
    }

    /**
     * Sets the test policy component (concrete instance inside the test case model) which is the
     * parent of the test association type.
     */
    public void setParentTestPolicyCmpt(ITestPolicyCmpt testPolicyCmpt) {
        parentTestPolicyCmpt = testPolicyCmpt;
    }

    /**
     * Returns the name of the test association type parameter (test policy component type
     * parameter).
     */
    public String getName() {
        if (testPolicyCmptTypeParameter == null) {
            return ""; //$NON-NLS-1$
        }
        return testPolicyCmptTypeParameter.getName();
    }

    /**
     * Returns the association name which is related by the test association type.
     */
    public String getAssociation() {
        return testPolicyCmptTypeParameter.getAssociation();
    }

    /**
     * Returns the association object which is related by the test association type.
     */
    public IPolicyCmptTypeAssociation findAssociation(IIpsProject ipsProject) throws CoreRuntimeException {
        return testPolicyCmptTypeParameter.findAssociation(ipsProject);
    }

    /**
     * Returns <code>true</code> if the test association type requires a product component.
     */
    public boolean isRequiresProductCmpt() {
        return testPolicyCmptTypeParameter.isRequiresProductCmpt();
    }

    /**
     * Returns <code>true</code> if the test association type is an input parameter.
     */
    public boolean isInput() {
        return testPolicyCmptTypeParameter.isInputOrCombinedParameter();
    }

    /**
     * Returns <code>true</code> if the test association type is an expected result parameter.
     */
    public boolean isExpectedResult() {
        return testPolicyCmptTypeParameter.isExpextedResultOrCombinedParameter();
    }

    /**
     * Returns the name of the policy component type which is related by the test association
     * parameter.
     */
    public String getPolicyCmptTypeTarget(IIpsProject ipsProject) throws CoreRuntimeException {
        return findAssociation(ipsProject).getTarget();
    }

    /**
     * Returns all test attributes which are defined inside the test association type (test policy
     * component type parameter).
     */
    public ITestAttribute[] getTestAttributes() {
        return testPolicyCmptTypeParameter.getTestAttributes();
    }

    //
    // Methods for validation interface
    //

    @Override
    public Severity getValidationResultSeverity(IIpsProject ipsProject) throws CoreRuntimeException {
        // TODO Auto-generated method stub
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
     * Validate the test policy cmpt association parameters. And validate the min and max instances
     * of the test policy cmpt type param by validating the parent test policy cmpt
     */
    private void validate(MessageList list, IIpsProject ipsProject) throws CoreRuntimeException {
        if (parentTestPolicyCmpt == null) {
            return;
        }

        /*
         * delegate the validation to the corresponding test policy component association validate
         * all associations with the same name, because this associations are grouped to one element
         */
        ITestPolicyCmptLink[] associations = parentTestPolicyCmpt.getTestPolicyCmptLinks();
        HashMap<String, Message> messages = new HashMap<>();
        for (ITestPolicyCmptLink association : associations) {
            ITestPolicyCmptLink testPolicyCmptAssociation = null;
            if (association.getTestPolicyCmptTypeParameter().equals(getName())) {
                testPolicyCmptAssociation = association;
                MessageList msgList = testPolicyCmptAssociation.validate(ipsProject);
                // add only unique messages
                for (Message msg : msgList) {
                    messages.put(msg.getCode(), msg);
                }
            }
        }

        // get the validation messages of the number of instances from the parent test policy cmpt,
        // thus it could be displayed on the association symbol
        MessageList ml = parentTestPolicyCmpt.validate(ipsProject);

        MessageList mlMin = ml
                .getMessagesFor(parentTestPolicyCmpt, ITestPolicyCmptTypeParameter.PROPERTY_MIN_INSTANCES);
        for (Message msg : mlMin) {
            if (ifMessageRelevant(msg)) {
                messages.put(msg.getCode(), msg);
            }
        }
        MessageList mlMax = ml
                .getMessagesFor(parentTestPolicyCmpt, ITestPolicyCmptTypeParameter.PROPERTY_MAX_INSTANCES);
        for (Message msg : mlMax) {
            if (ifMessageRelevant(msg)) {
                messages.put(msg.getCode(), msg);
            }
        }

        // add the unique test association messages to the list of messages
        for (Message message : messages.values()) {
            list.add(message);
        }
    }

    /**
     * Returns true if the message is relevant for this association.
     */
    private boolean ifMessageRelevant(Message msg) {
        return (msg.getText().indexOf("\"" + getName() + "\"") >= 0); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public ITestParameter getTestParameter() {
        return getTestPolicyCmptTypeParam();
    }

    @Override
    public IIpsProject getIpsProject() {
        return testPolicyCmptTypeParameter.getIpsProject();
    }

    public boolean isRoot() {
        return getParentTestPolicyCmpt() == null;
    }

}
