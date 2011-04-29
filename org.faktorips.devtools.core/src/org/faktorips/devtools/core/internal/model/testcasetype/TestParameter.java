/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.testcasetype;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
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
        return type.equals(TestParameterType.INPUT) || type.equals(TestParameterType.COMBINED);
    }

    @Override
    public boolean isExpextedResultOrCombinedParameter() {
        return type.equals(TestParameterType.EXPECTED_RESULT) || type.equals(TestParameterType.COMBINED);
    }

    @Override
    public boolean isCombinedParameter() {
        return type.equals(TestParameterType.COMBINED);
    }

    @Override
    public TestParameterType getTestParameterType() {
        return type;
    }

    @Override
    public abstract void setTestParameterType(TestParameterType testParameterType);

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

        // check for duplicate test parameter names
        ITestParameter[] testParameters = null;
        if (isRoot()) {
            testParameters = ((ITestCaseType)getParent()).getTestParameters();
        } else {
            // get all elements on the same level (all children of the parent object)
            IIpsElement[] childrenOfParent = ((ITestParameter)getParent()).getChildren();
            List<ITestParameter> testParameterChildrenOfParent = new ArrayList<ITestParameter>(childrenOfParent.length);
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
                    String text = NLS.bind(Messages.TestParameter_ValidationError_DuplicateName, name);
                    Message msg = new Message(MSGCODE_DUPLICATE_NAME, text, Message.ERROR, this, PROPERTY_NAME);
                    list.add(msg);
                    break;
                }
            }
        }

        // check the correct name format
        IStatus status = ValidationUtils.validateFieldName(name, ipsProject);
        if (!status.isOK()) {
            String text = NLS.bind(Messages.TestParameter_ValidateError_InvalidTestParamName, name);
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
