/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.testcasetype;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.ValidationUtils;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestParameter;
import org.faktorips.devtools.model.testcasetype.TestParameterType;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test parameter class. Superclass for all test parameter.
 * 
 * @author Joerg Ortmann
 */
public abstract class TestParameter extends IpsObjectPart implements ITestParameter {

    protected TestParameterType type = TestParameterType.COMBINED;

    public TestParameter(IIpsObjectPartContainer parent, String id) {
        super(parent, id);
    }

    @Override
    public abstract boolean isRoot();

    @Override
    public abstract ITestParameter getRootParameter();

    @Override
    public void setName(String newName) {
        String oldName = name;
        name = newName;
        valueChanged(oldName, newName);
    }

    @Override
    protected Element createElement(Document doc) {
        throw new RuntimeException("Not implemented!"); //$NON-NLS-1$
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute(PROPERTY_NAME);
        type = TestParameterType.getTestParameterType(element.getAttribute(PROPERTY_TEST_PARAMETER_TYPE));
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_TEST_PARAMETER_TYPE, type.getId());
    }

    @Override
    public boolean isInputOrCombinedParameter() {
        return type == TestParameterType.INPUT || type == TestParameterType.COMBINED;
    }

    @Override
    public boolean isExpextedResultOrCombinedParameter() {
        return TestParameterType.EXPECTED_RESULT == type || TestParameterType.COMBINED == type;
    }

    @Override
    public boolean isCombinedParameter() {
        return type == TestParameterType.COMBINED;
    }

    @Override
    public TestParameterType getTestParameterType() {
        return type;
    }

    @Override
    public abstract void setTestParameterType(TestParameterType testParameterType);

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        super.validateThis(list, ipsProject);

        // check for duplicate test parameter names
        ITestParameter[] testParameters = null;
        if (isRoot()) {
            testParameters = ((ITestCaseType)getParent()).getTestParameters();
        } else {
            // get all elements on the same level (all children of the parent object)
            IIpsElement[] childrenOfParent = ((ITestParameter)getParent()).getChildren();
            List<ITestParameter> testParameterChildrenOfParent = new ArrayList<>(childrenOfParent.length);
            for (IIpsElement element : childrenOfParent) {
                if (element instanceof ITestParameter) {
                    testParameterChildrenOfParent.add((ITestParameter)element);
                }
            }
            testParameters = testParameterChildrenOfParent.toArray(new ITestParameter[0]);
        }

        if (testParameters != null) {
            for (ITestParameter testParameter : testParameters) {
                if (testParameter != this && testParameter.getName().equals(name)) {
                    String text = MessageFormat.format(Messages.TestParameter_ValidationError_DuplicateName, name);
                    Message msg = new Message(MSGCODE_DUPLICATE_NAME, text, Message.ERROR, this, PROPERTY_NAME);
                    list.add(msg);
                    break;
                }
            }
        }

        // check the correct name format
        if (!ValidationUtils.validateFieldName(name, ipsProject)) {
            String text = MessageFormat.format(Messages.TestParameter_ValidateError_InvalidTestParamName, name);
            Message msg = new Message(MSGCODE_INVALID_NAME, text, Message.ERROR, this, PROPERTY_NAME);
            list.add(msg);
        }
    }

    /**
     * Return the test case type this parameter belongs to.
     */
    public TestCaseType getTestCaseType() {
        if (isRoot()) {
            return (TestCaseType)getParent();
        }
        ITestParameter root = getRootParameter();
        return (TestCaseType)root.getParent();
    }

}
