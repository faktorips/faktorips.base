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
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test value class. Defines a test value for a specific test case definition.
 * 
 * @author Joerg Ortmann
 */
public class TestValue extends TestObject implements ITestValue {

    /** Tags */
    final static String TAG_NAME = "ValueObject"; //$NON-NLS-1$

    private String testValueParameter = ""; //$NON-NLS-1$

    private String value = ""; //$NON-NLS-1$

    public TestValue(IIpsObject parent, int id) {
        super(parent, id);
    }

    public TestValue(IIpsObjectPart parent, int id) {
        super(parent, id);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, Integer id) {
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
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("TestValue.gif"); //$NON-NLS-1$
    }

    @Override
    public boolean isRoot() {
        // test values are always root elements
        return true;
    }

    public String getTestValueParameter() {
        return testValueParameter;
    }

    public void setTestValueParameter(String testValueParameter) {
        String oldTValueParameter = this.testValueParameter;
        this.testValueParameter = testValueParameter;
        valueChanged(oldTValueParameter, testValueParameter);
    }

    public String getTestParameterName() {
        return testValueParameter;
    }

    public ITestParameter findTestParameter(IIpsProject ipsProject) throws CoreException {
        return findTestValueParameter(ipsProject);
    }

    public ITestValueParameter findTestValueParameter(IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(testValueParameter)) {
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

    public String getValue() {
        return value;
    }

    public void setValue(String newValue) {
        String oldValue = value;
        value = newValue;
        valueChanged(oldValue, newValue);
    }

    public void setDefaultValue() throws CoreException {
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
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        ITestValueParameter param = findTestValueParameter(ipsProject);
        if (param == null) {
            String text = NLS.bind(Messages.TestValue_ValidateError_TestValueParamNotFound, getTestValueParameter());
            Message msg = new Message(MSGCODE_TEST_VALUE_PARAM_NOT_FOUND, text, Message.ERROR, this, PROPERTY_VALUE);
            list.add(msg);
        } else {
            // validate test parameter aspects will be severity warning

            // validate the test datatype value
            ValueDatatype datatype = param.findValueDatatype(ipsProject);
            if (datatype == null) {
                String text = NLS.bind(Messages.TestValue_ValidateError_DatatypeNotFound, param.getValueDatatype());
                Message msg = new Message(ITestValueParameter.MSGCODE_VALUEDATATYPE_NOT_FOUND, text, Message.WARNING,
                        this, PROPERTY_VALUE);
                list.add(msg);
            } else {
                ValidationUtils.checkValue(param.getDatatype(), value, this, PROPERTY_VALUE, list);
            }

            // validate the correct type of the test value parameter
            if (param.isCombinedParameter() || (!isInput() && !isExpectedResult())) {
                String text = NLS.bind(Messages.TestValue_ErrorWrongType, param.getName());
                Message msg = new Message(ITestValueParameter.MSGCODE_WRONG_TYPE, text, Message.WARNING, this,
                        ITestParameter.PROPERTY_TEST_PARAMETER_TYPE);
                list.add(msg);
            }
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
        return getTestValueParameter();
    }

    public RenameRefactoring getRenameRefactoring() {
        return null;
    }

    public boolean isRenameRefactoringSupported() {
        return false;
    }

}
