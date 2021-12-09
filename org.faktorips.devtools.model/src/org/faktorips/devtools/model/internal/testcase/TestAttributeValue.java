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

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.ValidationUtils;
import org.faktorips.devtools.model.internal.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.AttributeType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.testcasetype.TestParameterType;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test attribute value class. Defines an attribute value for a specific policy component class
 * within a test case definition.
 * 
 * @author Joerg Ortmann
 */
public class TestAttributeValue extends AtomicIpsObjectPart implements ITestAttributeValue {

    public static final String TAG_NAME = "AttributeValue"; //$NON-NLS-1$

    /**
     * Specifies the default type, will be used if the corresponding test case type parameter is not
     * specified or not found
     */
    private static final TestParameterType DEFAULT_TYPE = TestParameterType.COMBINED;

    private String testAttribute = ""; //$NON-NLS-1$

    private String value = ""; //$NON-NLS-1$

    public TestAttributeValue(IIpsObjectPartContainer parent, String id) {
        super(parent, id);
    }

    /**
     * Returns the parent test policy cmpt.
     */
    public ITestPolicyCmpt getTestPolicyCmpt() {
        return (ITestPolicyCmpt)getParent();
    }

    @Override
    public String getTestAttribute() {
        return testAttribute;
    }

    @Override
    public void setTestAttribute(String testAttribute) {
        String oldTestAttribute = this.testAttribute;
        this.testAttribute = testAttribute;
        valueChanged(oldTestAttribute, testAttribute);
    }

    @Override
    public ITestAttribute findTestAttribute(IIpsProject ipsProject) throws CoreRuntimeException {
        if (StringUtils.isEmpty(testAttribute)) {
            return null;
        }
        ITestPolicyCmpt testPolicyCmpt = getTestPolicyCmpt();
        ITestPolicyCmptTypeParameter typeParam = testPolicyCmpt.findTestPolicyCmptTypeParameter(ipsProject);
        if (typeParam == null) {
            return null;
        }
        return typeParam.getTestAttribute(testAttribute);
    }

    @Override
    public IAttribute findAttribute(IIpsProject ipsProject) throws CoreRuntimeException {
        ITestAttribute testAttr = findTestAttribute(ipsProject);
        if (testAttr == null) {
            return null;
        }

        ITestPolicyCmpt testPolicyCmpt = getTestPolicyCmpt();
        if (!StringUtils.isEmpty(testPolicyCmpt.getPolicyCmptType())) {
            IPolicyCmptType policyCmptType = testPolicyCmpt.findPolicyCmptType();
            if (policyCmptType == null) {
                return null;
            }
            return policyCmptType.findAttribute(testAttr.getAttribute(), ipsProject);
        }

        if (!testPolicyCmpt.isProductRelevant()) {
            return testAttr.findAttribute(ipsProject);
        }
        return testPolicyCmpt.findProductCmptTypeAttribute(testAttr.getAttribute(), ipsProject);
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
    public void setDefaultValue() throws CoreRuntimeException {
        IAttribute modelAttribute = findAttribute(getIpsProject());
        if (modelAttribute == null) {
            return;
        }

        if (isInputAttribute(getIpsProject())) {
            setValue(modelAttribute.getDefaultValue());
        } else {
            ValueDatatype datatype = modelAttribute.findDatatype(getIpsProject());
            if (datatype != null) {
                setValue(datatype.getDefaultValue());
            }
        }
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        testAttribute = element.getAttribute(PROPERTY_ATTRIBUTE);
        // PROPERTY_VALUE is not used because the first character must be upper case
        value = ValueToXmlHelper.getValueFromElement(element, "Value"); //$NON-NLS-1$
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_ATTRIBUTE, testAttribute);
        ValueToXmlHelper.addValueToElement(value, element, "Value"); //$NON-NLS-1$
    }

    @Override
    public boolean isExpectedResultAttribute(IIpsProject ipsProject) {
        return isTypeOrDefault(TestParameterType.EXPECTED_RESULT, DEFAULT_TYPE, ipsProject);
    }

    @Override
    public boolean isInputAttribute(IIpsProject ipsProject) {
        return isTypeOrDefault(TestParameterType.INPUT, DEFAULT_TYPE, ipsProject);
    }

    /**
     * Returns <code>true</code> if the corresponding test attribute if of the given type. If the
     * test attribute can't be found, return <code>true</code> if the given type is the default type
     * otherwise <code>false</code>.<br>
     * Return <code>false</code> if an error occurs.<br>
     */
    private boolean isTypeOrDefault(TestParameterType type, TestParameterType defaultType, IIpsProject ipsProject) {
        try {
            TestObject parent = (TestObject)getParent();
            ITestCase testCase = (TestCase)parent.getRoot().getParent();

            ITestCaseType testCaseType = testCase.findTestCaseType(ipsProject);
            if (testCaseType == null) {
                return type.equals(defaultType);
            }

            ITestAttribute attribute = findTestAttribute(ipsProject);
            if (testAttribute == null) {
                return type.equals(defaultType);
            }

            // compare the parameters type and return if the type matches the given type
            if (attribute.isInputAttribute() && type.equals(TestParameterType.INPUT)) {
                return true;
            }
            if (attribute.isExpextedResultAttribute() && type.equals(TestParameterType.EXPECTED_RESULT)) {
                return true;
            }
            // CSOFF: Empty Statement
        } catch (CoreRuntimeException e) {
            // ignore exceptions
        }
        // CSON: Empty Statement
        return false;
    }

    @Override
    public void updateDefaultTestAttributeValue() throws CoreRuntimeException {
        IProductCmptGeneration generation = ((TestPolicyCmpt)getParent()).findProductCmpsCurrentGeneration(getParent()
                .getIpsProject());
        setDefaultTestAttributeValueInternal(generation);
    }

    /**
     * Updates the default for the test attribute value. The default will be retrieved from the
     * product cmpt or if no product cmpt is available or the attribute isn't configurated by
     * product then from the policy cmpt. Don't update the value if not default is specified.
     */
    void setDefaultTestAttributeValueInternal(IProductCmptGeneration generation) throws CoreRuntimeException {
        IIpsProject ipsProject = getIpsProject();
        ITestAttribute attribute = findTestAttribute(ipsProject);
        if (attribute == null) {
            // the test attribute wasn't found, do nothing
            // this is an error which will be validated in the validate method
            return;
        }

        IPolicyCmptTypeAttribute modelAttribute = attribute.findAttribute(ipsProject);
        if (modelAttribute != null) {
            boolean defaultSet = false;
            // set default as specified in the product cmpt
            // if attribute is product relevant, a generation exists
            // and the attribute is changeable
            if (modelAttribute.isProductRelevant() && generation != null
                    && modelAttribute.getAttributeType().equals(AttributeType.CHANGEABLE)) {
                IConfiguredDefault ce = generation.getConfiguredDefault(modelAttribute.getName());
                if (ce != null) {
                    setValue(ce.getValue());
                    defaultSet = true;
                }
            }
            // alternative set the default as specified in the policy cmpt type
            if (!defaultSet) {
                setValue(modelAttribute.getDefaultValue());
                defaultSet = true;
            }
        } else {
            // the model attribute (policy cmpt type attribute) wasn't found,
            // set the default using the datatype
            ValueDatatype datatype = attribute.findDatatype(ipsProject);
            if (datatype != null) {
                setValue(datatype.getDefaultValue());
            } else {
                setValue(null);
            }
        }
    }

    @Override
    protected void validateThis(MessageList messageList, IIpsProject ipsProject) throws CoreRuntimeException {
        super.validateThis(messageList, ipsProject);
        ITestAttribute testAttr = findTestAttribute(ipsProject);
        if (testAttr == null) {
            String text = MessageFormat.format(Messages.TestAttributeValue_ValidateError_TestAttributeNotFound,
                    getTestAttribute());
            Message msg = new Message(MSGCODE_TESTATTRIBUTE_NOT_FOUND, text, Message.ERROR, this, PROPERTY_VALUE);
            messageList.add(msg);
        } else {
            if (testAttr.isBasedOnModelAttribute()) {
                IAttribute attribute = findAttribute(ipsProject);
                /*
                 * create a warning only if the attribute wasn't found and the value is set,
                 * otherwise the attribute value object is disabled (not relevant for the test
                 * policy cmpt), because the policy cmpt could be a subclass of the policy cmpt
                 * which is defined in the test case type, and the attribute should be only relevant
                 * for other policy cmpts which defines this subclass attribute
                 */
                if (attribute == null && !StringUtils.isEmpty(value)) {
                    String text = MessageFormat.format(Messages.TestAttributeValue_ValidateError_AttributeNotFound,
                            testAttr.getAttribute());
                    Message msg = new Message(ITestAttribute.MSGCODE_ATTRIBUTE_NOT_FOUND, text, Message.WARNING, this,
                            PROPERTY_VALUE);
                    messageList.add(msg);
                }

                if (attribute != null) {
                    // ignore validation if the attribute wasn't found (see above)
                    ValidationUtils.checkValue(attribute.getDatatype(), value, this, PROPERTY_VALUE, messageList);
                }
            } else {
                // the test attribute is not based on a model attribute therefor check only the
                // value
                ValidationUtils.checkValue(testAttr.getDatatype(), value, this, PROPERTY_VALUE, messageList);
            }

            // check the correct type
            if (!testAttr.isInputAttribute() && !testAttr.isExpextedResultAttribute()) {
                String text = MessageFormat.format(Messages.TestAttributeValue_Error_WrongType, testAttr.getName());
                Message msg = new Message(ITestAttribute.MSGCODE_WRONG_TYPE, text, Message.WARNING, this,
                        PROPERTY_VALUE);
                messageList.add(msg);
            }
        }
    }

    @Override
    public String getName() {
        return getTestAttribute();
    }

}
