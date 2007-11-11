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
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
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
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test attribute value class. 
 * Defines an attribute value for a specific policy component class within a test case definition.
 * 
 * @author Joerg Ortmann
 */
public class TestAttributeValue  extends AtomicIpsObjectPart implements ITestAttributeValue {
    /* Specifies the default type, will be used if the corresponding test case type parameter 
     * is not specified or not found */
    private static TestParameterType DEFAULT_TYPE = TestParameterType.COMBINED;
    
	/* Tags */
	static final String TAG_NAME = "AttributeValue"; //$NON-NLS-1$

	private String testAttribute = ""; //$NON-NLS-1$
	
	private String value = ""; //$NON-NLS-1$
	
	public TestAttributeValue(IIpsObject parent, int id) {
		super(parent, id);
	}

	public TestAttributeValue(IIpsObjectPart parent, int id) {
		super(parent, id);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getTestAttribute() {
		return testAttribute;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTestAttribute(String testAttribute) {
		String oldTestAttribute = this.testAttribute;
        this.testAttribute = testAttribute;
        valueChanged(oldTestAttribute, testAttribute);
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestAttribute findTestAttribute() throws CoreException {
        if (StringUtils.isEmpty(testAttribute)) {
            return null;
        }
        ITestPolicyCmpt testPolicyCmpt = (ITestPolicyCmpt) getParent();
        ITestPolicyCmptTypeParameter typeParam = testPolicyCmpt.findTestPolicyCmptTypeParameter();
        if (typeParam == null){
        	return null;
        }
        return typeParam.getTestAttribute(testAttribute);
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
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    /**
     * {@inheritDoc}
     */
	protected void initPropertiesFromXml(Element element, Integer id) {
		super.initPropertiesFromXml(element, id);
		testAttribute = element.getAttribute(PROPERTY_ATTRIBUTE);
		value = ValueToXmlHelper.getValueFromElement(element, "Value"); //$NON-NLS-1$
        if (value == null){
            // TODO Joerg: Workaround for existing test cases
            value = ValueToXmlHelper.getValueFromElement(element, PROPERTY_VALUE);
        }        
	}

    /**
     * {@inheritDoc}
     */
	protected void propertiesToXml(Element element) {
		super.propertiesToXml(element);
		element.setAttribute(PROPERTY_ATTRIBUTE, testAttribute);
		ValueToXmlHelper.addValueToElement(value, element, "Value"); //$NON-NLS-1$
	}  
	
    /** 
     * {@inheritDoc}
     */
    public Image getImage() {
        try {
            ITestAttribute testAttribute = findTestAttribute();
            if (testAttribute != null){
                return testAttribute.getImage();
            }            
        } catch (CoreException e) {
            // ignore exception, return default image
        }
        return IpsPlugin.getDefault().getImage("MissingAttribute.gif"); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
	public boolean isExpextedResultAttribute() {
        return (isTypeOrDefault(TestParameterType.EXPECTED_RESULT, DEFAULT_TYPE));
    }

    /**
     * {@inheritDoc}
     */
    public boolean isInputAttribute() {
        return (isTypeOrDefault(TestParameterType.INPUT, DEFAULT_TYPE));
    }
    
    /**
     * Returns <code>true</code> if the given type is the type of the corresponding test
     * attribute. If the test attribute couldn't determined return <code>true</code> if the given
     * type is the default type otherwise <code>false</code>.<br>
     * Return <code>false</code> if an error occurs.<br>
     */
    private boolean isTypeOrDefault(TestParameterType type, TestParameterType defaultType) {
        try {
            TestObject parent = (TestObject) getParent(); 
            ITestCase testCase = (TestCase) parent.getRoot().getParent();
            
            ITestCaseType testCaseType = testCase.findTestCaseType();
            if (testCaseType == null)
                return type.equals(defaultType);

            ITestAttribute testAttribute = findTestAttribute();
            if (testAttribute == null)
                return type.equals(defaultType);

            // compare the paramters type and return if the type matches the given type
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
    
    /**
     * {@inheritDoc}
     */
    public void updateDefaultTestAttributeValue() throws CoreException {
        IProductCmptGeneration generation = ((TestPolicyCmpt)getParent()).findProductCmpsCurrentGeneration();
        setDefaultTestAttributeValueInternal(generation);
    }

    /**
     * Updates the default for the test attribute value. The default will be retrieved from the
     * product cmpt or if no product cmpt is available or the attribute isn't configurated by product 
     * then from the policy cmpt. Don't update the value if not default is specified.
     */
    void setDefaultTestAttributeValueInternal(IProductCmptGeneration generation) throws CoreException {
        ITestAttribute testAttribute = findTestAttribute();
        if (testAttribute == null) {
            // the test attribute wasn't found, do nothing
            // this is an error which will be validated in the validate method
            return;
        }
        
        IPolicyCmptTypeAttribute modelAttribute = testAttribute.findAttribute();
        if (modelAttribute != null){
            boolean defaultSet = false;
            // set default as specified in the product cmpt 
            // if attribute is product relevant, a generation exists 
            // and the attribute is changeable
            if (modelAttribute.isProductRelevant() && generation != null
                    && modelAttribute.getAttributeType().equals(AttributeType.CHANGEABLE)) {
                IConfigElement ce = generation.getConfigElement(modelAttribute.getName());
                if (ce != null) {
                    this.setValue(ce.getValue());
                    defaultSet = true;
                }
            }
            // alternative set the default as specified in the policy cmpt type
            if (! defaultSet){
                this.setValue(modelAttribute.getDefaultValue());
                defaultSet = true;
            }
        } else {
            // the model attribute (policy cmpt type attribute) wasn't found, do nothing
        }
    }
    
    /**
	 * {@inheritDoc}
	 */
	protected void validateThis(MessageList messageList, IIpsProject ipsProject) throws CoreException {
		super.validateThis(messageList, ipsProject);
        ITestAttribute testAttr = findTestAttribute();
        if (testAttr == null) {
            String text = NLS.bind(Messages.TestAttributeValue_ValidateError_TestAttributeNotFound, getTestAttribute());
            Message msg = new Message(MSGCODE_TESTATTRIBUTE_NOT_FOUND, text, Message.ERROR, this, PROPERTY_VALUE);
            messageList.add(msg);
        } else {
            IPolicyCmptTypeAttribute attribute = testAttr.findAttribute();
            if (attribute == null){
                // attribute not found in test case type definition, maybe this is a concrete subclass with additional attributes,
                // therefore try to find the attribute by using the product cmpt (product cmpt type the product cmpt is based on)
                attribute = ((ITestPolicyCmpt)getParent()).findProductCmptAttribute(testAttr.getAttribute());
            }
            // create a warning only if the attribute wasn't found and the value is set,
            // otherwise the attribute value object is disabled (not relevant for the test policy cmpt),
            // because the policy cmpt could be a subclass of the policy cmpt which is defined in the test case type,
            // and the attribute should be only relevant for other policy cmpts which defines this subclass attribute
            if (attribute == null && !StringUtils.isEmpty(value)) {
                String text = NLS.bind(Messages.TestAttributeValue_ValidateError_AttributeNotFound, testAttr
                        .getAttribute());
                Message msg = new Message(ITestAttribute.MSGCODE_ATTRIBUTE_NOT_FOUND, text, Message.WARNING, this,
                        PROPERTY_VALUE);
                messageList.add(msg);
            }
            
            if (attribute != null ){
                // ignore validation if the attribute wasn't found (see above)
                ValidationUtils.checkValue(attribute.getDatatype(), value, this, PROPERTY_VALUE, messageList);
            }
            
            // check the correct type
            if (! testAttr.isInputAttribute() && ! testAttr.isExpextedResultAttribute()){
                String text = NLS.bind(Messages.TestAttributeValue_Error_WrongType, testAttr.getName());
                Message msg = new Message(ITestAttribute.MSGCODE_WRONG_TYPE, text, Message.WARNING, this, PROPERTY_VALUE);
                messageList.add(msg);
            }        
        }
	}

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return getTestAttribute();
    }
}
