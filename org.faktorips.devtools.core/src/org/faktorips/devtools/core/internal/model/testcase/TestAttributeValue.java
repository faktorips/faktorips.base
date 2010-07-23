/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.testcase;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test attribute value class. Defines an attribute value for a specific policy component class
 * within a test case definition.
 * 
 * @author Joerg Ortmann
 */
public class TestAttributeValue extends AtomicIpsObjectPart implements ITestAttributeValue {

    /**
     * Specifies the default type, will be used if the corresponding test case type parameter is not
     * specified or not found
     */
    private static TestParameterType DEFAULT_TYPE = TestParameterType.COMBINED;

    static final String TAG_NAME = "AttributeValue"; //$NON-NLS-1$

    private String testAttribute = ""; //$NON-NLS-1$

    private String value = ""; //$NON-NLS-1$

    public TestAttributeValue(IIpsObjectPartContainer parent, String id) {
        super(parent, id);
        descriptionChangable = false;
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
    public ITestAttribute findTestAttribute(IIpsProject ipsProject) throws CoreException {
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
    public IAttribute findAttribute(IIpsProject ipsProject) throws CoreException {
        ITestAttribute testAttr = findTestAttribute(ipsProject);
        if (testAttr == null) {
            return null;
        }

        ITestPolicyCmpt testPolicyCmpt = getTestPolicyCmpt();
        if (!StringUtils.isEmpty(testPolicyCmpt.getPolicyCmptType())) {
            IPolicyCmptType policyCmptType = testPolicyCmpt.findPolicyCmptType();
            return policyCmptType.findAttribute(testAttr.getAttribute(), ipsProject);
        }

        if (!testPolicyCmpt.isProductRelevant()) {
            return testAttr.findAttribute(ipsProject);
        } else {
            return testPolicyCmpt.findProductCmptTypeAttribute(testAttr.getAttribute(), ipsProject);
        }
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
    public void setDefaultValue() throws CoreException {
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
    public boolean isExpextedResultAttribute(IIpsProject ipsProject) {
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

            ITestAttribute testAttribute = findTestAttribute(ipsProject);
            if (testAttribute == null) {
                return type.equals(defaultType);
            }

            // compare the parameters type and return if the type matches the given type
            if (testAttribute.isInputAttribute() && type.equals(TestParameterType.INPUT)) {
                return true;
            }
            if (testAttribute.isExpextedResultAttribute() && type.equals(TestParameterType.EXPECTED_RESULT)) {
                return true;
            }
        } catch (Exception e) {
            // ignore exceptions
        }
        return false;
    }

    @Override
    public void updateDefaultTestAttributeValue() throws CoreException {
        IProductCmptGeneration generation = ((TestPolicyCmpt)getParent()).findProductCmpsCurrentGeneration(getParent()
                .getIpsProject());
        setDefaultTestAttributeValueInternal(generation);
    }

    /**
     * Updates the default for the test attribute value. The default will be retrieved from the
     * product cmpt or if no product cmpt is available or the attribute isn't configurated by
     * product then from the policy cmpt. Don't update the value if not default is specified.
     */
    void setDefaultTestAttributeValueInternal(IProductCmptGeneration generation) throws CoreException {
        IIpsProject ipsProject = getIpsProject();
        ITestAttribute testAttribute = findTestAttribute(ipsProject);
        if (testAttribute == null) {
            // the test attribute wasn't found, do nothing
            // this is an error which will be validated in the validate method
            return;
        }

        IPolicyCmptTypeAttribute modelAttribute = testAttribute.findAttribute(ipsProject);
        if (modelAttribute != null) {
            boolean defaultSet = false;
            // set default as specified in the product cmpt
            // if attribute is product relevant, a generation exists
            // and the attribute is changeable
            if (modelAttribute.isProductRelevant() && generation != null
                    && modelAttribute.getAttributeType().equals(AttributeType.CHANGEABLE)) {
                IConfigElement ce = generation.getConfigElement(modelAttribute.getName());
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
            ValueDatatype datatype = testAttribute.findDatatype(ipsProject);
            if (datatype != null) {
                setValue(datatype.getDefaultValue());
            } else {
                setValue(null);
            }
        }
    }

    @Override
    protected void validateThis(MessageList messageList, IIpsProject ipsProject) throws CoreException {
        super.validateThis(messageList, ipsProject);
        ITestAttribute testAttr = findTestAttribute(ipsProject);
        if (testAttr == null) {
            String text = NLS.bind(Messages.TestAttributeValue_ValidateError_TestAttributeNotFound, getTestAttribute());
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
                    String text = NLS.bind(Messages.TestAttributeValue_ValidateError_AttributeNotFound, testAttr
                            .getAttribute());
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
                String text = NLS.bind(Messages.TestAttributeValue_Error_WrongType, testAttr.getName());
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
