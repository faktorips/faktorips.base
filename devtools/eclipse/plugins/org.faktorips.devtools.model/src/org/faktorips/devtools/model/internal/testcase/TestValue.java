/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.testcase;

import java.text.MessageFormat;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.ValidationUtils;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcase.ITestObject;
import org.faktorips.devtools.model.testcase.ITestValue;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestParameter;
import org.faktorips.devtools.model.testcasetype.ITestValueParameter;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test value class. Defines a test value for a specific test case definition.
 * 
 * @author Joerg Ortmann
 */
public class TestValue extends TestObject implements ITestValue {

    static final String TAG_NAME = "ValueObject"; //$NON-NLS-1$

    private String testValueParameter = ""; //$NON-NLS-1$

    private String value = ""; //$NON-NLS-1$

    public TestValue(IIpsObjectPartContainer parent, String id) {
        super(parent, id);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        testValueParameter = element.getAttribute(PROPERTY_VALUE_PARAMETER);
        value = ValueToXmlHelper.getValueFromElement(element, "Value"); //$NON-NLS-1$
        if (value == null) {
            // TODO Joerg: Workaround for existing test cases
            value = ValueToXmlHelper.getValueFromElement(element, PROPERTY_VALUE);
        }
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_VALUE_PARAMETER, testValueParameter);
        ValueToXmlHelper.addValueToElement(value, element, "Value"); //$NON-NLS-1$
    }

    @Override
    public boolean isRoot() {
        // test values are always root elements
        return true;
    }

    @Override
    public String getTestValueParameter() {
        return testValueParameter;
    }

    @Override
    public void setTestValueParameter(String testValueParameter) {
        String oldTValueParameter = this.testValueParameter;
        this.testValueParameter = testValueParameter;
        valueChanged(oldTValueParameter, testValueParameter);
    }

    @Override
    public String getTestParameterName() {
        return testValueParameter;
    }

    @Override
    public ITestParameter findTestParameter(IIpsProject ipsProject) {
        return findTestValueParameter(ipsProject);
    }

    @Override
    public ITestValueParameter findTestValueParameter(IIpsProject ipsProject) {
        if (IpsStringUtils.isEmpty(testValueParameter)) {
            return null;
        }

        ITestCaseType testCaseType = ((ITestCase)getParent()).findTestCaseType(ipsProject);
        if (testCaseType == null) {
            return null;
        }
        ITestParameter param = testCaseType.getTestParameterByName(testValueParameter);
        if (param instanceof ITestValueParameter) {
            return (ITestValueParameter)param;
        }
        return null;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String newValue) {
        String oldValue = value;
        value = newValue;
        valueChanged(oldValue, newValue);
    }

    @Override
    public void setDefaultValue() {
        ITestValueParameter parameter = findTestValueParameter(getIpsProject());
        if (parameter == null) {
            return;
        }
        ValueDatatype valueDatatype = parameter.findValueDatatype(getIpsProject());
        if (valueDatatype != null) {
            setValue(valueDatatype.getDefaultValue());
        }
    }

    @Override
    public ITestObject getRoot() {
        // test values have no childs
        return this;
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        super.validateThis(list, ipsProject);
        ITestValueParameter param = findTestValueParameter(ipsProject);
        if (param == null) {
            String text = MessageFormat.format(Messages.TestValue_ValidateError_TestValueParamNotFound,
                    getTestValueParameter());
            Message msg = new Message(MSGCODE_TEST_VALUE_PARAM_NOT_FOUND, text, Message.ERROR, this, PROPERTY_VALUE);
            list.add(msg);
        } else {
            // validate test parameter aspects will be severity warning

            // validate the test datatype value
            ValueDatatype datatype = param.findValueDatatype(ipsProject);
            if (datatype == null) {
                String text = MessageFormat.format(Messages.TestValue_ValidateError_DatatypeNotFound,
                        param.getValueDatatype());
                Message msg = new Message(ITestValueParameter.MSGCODE_VALUEDATATYPE_NOT_FOUND, text, Message.WARNING,
                        this, PROPERTY_VALUE);
                list.add(msg);
            } else {
                ValidationUtils.checkValue(param.getDatatype(), value, this, PROPERTY_VALUE, list);
            }

            // validate the correct type of the test value parameter
            if (param.isCombinedParameter() || (!isInput() && !isExpectedResult())) {
                String text = MessageFormat.format(Messages.TestValue_ErrorWrongType, param.getName());
                Message msg = new Message(ITestValueParameter.MSGCODE_WRONG_TYPE, text, Message.WARNING, this,
                        ITestParameter.PROPERTY_TEST_PARAMETER_TYPE);
                list.add(msg);
            }
        }
    }

    @Override
    public String getName() {
        return getTestValueParameter();
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        return new IIpsElement[0];
    }

    @Override
    protected void reinitPartCollectionsThis() {
        // Nothing to do
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        return false;
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        return false;
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        return null;
    }

    @Override
    protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        return null;
    }

}
