/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.testcase;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
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
    
	/**
     * {@inheritDoc}
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    /**
     * {@inheritDoc}
     */
	protected void initPropertiesFromXml(Element element, Integer id) {
		super.initPropertiesFromXml(element, id);
		testValueParameter = element.getAttribute(PROPERTY_VALUE_PARAMETER);
		value = ValueToXmlHelper.getValueFromElement(element, PROPERTY_VALUE);
	}

    /**
     * {@inheritDoc}
     */
	protected void propertiesToXml(Element element) {
		super.propertiesToXml(element);
		element.setAttribute(PROPERTY_VALUE_PARAMETER, testValueParameter);
		ValueToXmlHelper.addValueToElement(value, element, PROPERTY_VALUE);
	}

    /**
     * {@inheritDoc}
     */
    public boolean isRoot() {
        // test values are always root elements
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String getTestValueParameter() {
        return testValueParameter;
    }

    /**
     * {@inheritDoc}
     */
    public void setTestValueParameter(String testValueParameter) {
        String oldTValueParameter = this.testValueParameter;
        this.testValueParameter = testValueParameter;
        valueChanged(oldTValueParameter, testValueParameter);
    }

    /**
     * {@inheritDoc}
     */
    protected String getTestParameterName() {
        return testValueParameter;
    } 
    
    /**
     * {@inheritDoc}
     */
    public ITestValueParameter findTestValueParameter() throws CoreException {
        if (StringUtils.isEmpty(testValueParameter)) {
            return null;
        }

        ITestCaseType testCaseType = ((ITestCase)getParent()).findTestCaseType();
        if (testCaseType == null)
            return null;

        return isInput() ? testCaseType.getInputTestValueParameter(testValueParameter) : testCaseType
                .getExpectedResultTestValueParameter(testValueParameter);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(String newValue) {
        String oldValue = this.value;
        this.value = newValue;
        valueChanged(oldValue, newValue);
    }
    
    /**
     * {@inheritDoc}
     */
	public ITestObject getRoot() {
        // test values have no childs
        return this;
    }

    /**
	 * {@inheritDoc}
	 */
	protected void validateThis(MessageList list) throws CoreException {
		super.validateThis(list);
        ITestValueParameter param = findTestValueParameter();
        if (param == null) {
            String text = NLS.bind(Messages.TestValue_ValidateError_TestValueParamNotFound, getTestValueParameter());
            Message msg = new Message(MSGCODE_TEST_VALUE_PARAM_NOT_FOUND, text, Message.ERROR, this, PROPERTY_VALUE);
            list.add(msg);
        } else {
            // validate the test datatype value
            ValueDatatype datatype = param.findValueDatatype();
            if (datatype == null) {
                String text = NLS.bind(Messages.TestValue_ValidateError_DatatypeNotFound, param.getValueDatatype());
                Message msg = new Message(ITestValueParameter.MSGCODE_VALUEDATATYPE_NOT_FOUND, text, Message.WARNING,
                        this, PROPERTY_VALUE);
                list.add(msg);
            } else if (!datatype.isParsable(value)) {
                String text = NLS.bind(Messages.TestValue_ValidateError_ValueIsNoDatatype, value, datatype);
                Message msg = new Message(MSGCODE_DATATYPEVALUE_NOT_PARSABLE, text, Message.ERROR, this, PROPERTY_VALUE);
                list.add(msg);
            }
            // validate the correct type of the test value parameter
            if (param.isCombinedParameter() || (!isInput() && !isExpectedResult())) {
                String text = NLS.bind(Messages.TestValue_ErrorWrongType, param.getName());
                Message msg = new Message(ITestValueParameter.MSGCODE_WRONG_TYPE, text, Message.WARNING, this,
                        ITestValueParameter.PROPERTY_TEST_PARAMETER_TYPE);
                list.add(msg);
            }
        }
	}
}
