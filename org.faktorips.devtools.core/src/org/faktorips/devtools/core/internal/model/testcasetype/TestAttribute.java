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

package org.faktorips.devtools.core.internal.model.testcasetype;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test attribute class. 
 * Defines an attribute for a specific policy component parameter class within a test case type definition.
 * 
 * @author Joerg Ortmann
 */
public class TestAttribute extends AtomicIpsObjectPart implements ITestAttribute {

	/* Tags */
	static final String TAG_NAME = "TestAttribute"; //$NON-NLS-1$
	
	private String attribute = ""; //$NON-NLS-1$
    
	private TestParameterType type = TestParameterType.COMBINED;
    
	public TestAttribute(IIpsObject parent, int id) {
		super(parent, id);
	}

	public TestAttribute(IIpsObjectPart parent, int id) {
		super(parent, id);
	}
	
    /** 
     * {@inheritDoc}
     */
    public String getName() {
        return name;
    }
    
    /** 
     * {@inheritDoc}
     */
    public void setName(String name) {
        String oldName = this.name;
        this.name = name;
        valueChanged(oldName, name);
    }
    
	/**
	 * {@inheritDoc}
	 */
	public String getAttribute() {
		return attribute;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAttribute(String newAttribute) {
		String oldAttribute = this.attribute;
		this.attribute = newAttribute;
		valueChanged(oldAttribute, newAttribute);
	}

	/**
	 * {@inheritDoc}
	 */
	public IPolicyCmptTypeAttribute findAttribute() throws CoreException {
        if (StringUtils.isEmpty(attribute)) {
            return null;
        }
        IPolicyCmptType pcType = ((TestPolicyCmptTypeParameter)getParent()).findPolicyCmptType();
        if (pcType == null){
            return null;
        }
        return pcType.findAttributeInSupertypeHierarchy(attribute);
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
        name = element.getAttribute(PROPERTY_NAME);
		attribute = element.getAttribute(PROPERTY_ATTRIBUTE);
		type = TestParameterType.getTestParameterType(element.getAttribute(PROPERTY_TEST_ATTRIBUTE_TYPE));
	}

    /**
     * {@inheritDoc}
     */
	protected void propertiesToXml(Element element) {
		super.propertiesToXml(element);
        element.setAttribute(PROPERTY_NAME, name);
		element.setAttribute(PROPERTY_ATTRIBUTE, attribute);
		element.setAttribute(PROPERTY_TEST_ATTRIBUTE_TYPE, type.getId());
	}  
    
    /** 
     * {@inheritDoc}
     */
    public Image getImage() {
        try {
            IPolicyCmptTypeAttribute attribute = findAttribute();
            if (attribute != null){
                return attribute.getImage();
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
		return type.equals(TestParameterType.EXPECTED_RESULT);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isInputAttribute() {
		return type.equals(TestParameterType.INPUT);
	}
    
    /**
     * {@inheritDoc}
     */
    public TestParameterType getTestAttributeType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    public void setTestAttributeType(TestParameterType type) {
        // assert that the given type is an input or an expected result type,
        // because attributes could have the type combined (input and expected result together)
        ArgumentCheck.isTrue(type.equals(TestParameterType.INPUT) || type.equals(TestParameterType.EXPECTED_RESULT));
        TestParameterType oldType = this.type;
        this.type = type;
        valueChanged(oldType, type);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isAttributeRelevantByProductCmpt(IProductCmpt productCmpt) throws CoreException {
        boolean reqProductCmpt = ((ITestPolicyCmptTypeParameter)getParent()).isRequiresProductCmpt();
        if (! reqProductCmpt){
            return true;
        }
        if (productCmpt == null){
            return false;
        } 
        IPolicyCmptType policyCmptType = productCmpt.findPolicyCmptType();
        return ! (policyCmptType.findAttributeInSupertypeHierarchy(getAttribute()) == null);
    }

    private IPolicyCmptTypeAttribute findAttributeInSubtypehierarchy() throws CoreException {
        if (StringUtils.isEmpty(attribute)) {
            return null;
        }
        IPolicyCmptType pcType = ((TestPolicyCmptTypeParameter)getParent()).findPolicyCmptType();

        if (pcType != null) {
            IPolicyCmptTypeAttribute[] attributes;
            List allAttributes = new ArrayList();
            ITypeHierarchy subtypeHierarchy = pcType.getSubtypeHierarchy();
            IPolicyCmptType[] allSubtypes = subtypeHierarchy.getAllSubtypes(pcType);
            for (int i = 0; i < allSubtypes.length; i++) {
                attributes = allSubtypes[i].getPolicyCmptTypeAttributes();
                for (int j = 0; j < attributes.length; j++) {
                    allAttributes.add(attributes[j]);
                }
            }
            attributes = (IPolicyCmptTypeAttribute[])allAttributes.toArray(new IPolicyCmptTypeAttribute[allAttributes.size()]);

            for (int i = 0; i < attributes.length; i++) {
                if (attributes[i].getName().equals(attribute)) {
                    return attributes[i];
                }
            }
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList messageList) throws CoreException {
        super.validateThis(messageList);
        
        // check if the name is not empty
        if (StringUtils.isEmpty(getName())){
            String text = Messages.TestAttribute_TestAttribute_Error_NameIsEmpty;
            Message msg = new Message(MSGCODE_ATTRIBUTE_NAME_IS_EMPTY, text, Message.ERROR, this, ITestAttribute.PROPERTY_NAME);
            messageList.add(msg);
        }
        
        // check if the attribute exists
        IPolicyCmptTypeAttribute modelAttribute = findAttribute();
        if (modelAttribute == null){
            // special case if the attribute wasn't found in the supertype then
            // search in the subtype hierarchy, the test case type attributes supports attributes of subclasses
            // therefore this special validation must be performed for such types of attributes only
            // Note: that this find method has a bad performance, therefore this should not the normal case
            modelAttribute = findAttributeInSubtypehierarchy();
        }
        if (modelAttribute == null){
            String text = NLS.bind(Messages.TestAttribute_Error_AttributeNotFound, getAttribute());
            Message msg = new Message(MSGCODE_ATTRIBUTE_NOT_FOUND, text, Message.ERROR, this, ITestAttribute.PROPERTY_ATTRIBUTE);
            messageList.add(msg);
        } 
        
        // check the correct type
        if (! isInputAttribute() && ! isExpextedResultAttribute()){
            String text = NLS.bind(Messages.TestAttribute_Error_TypeNotAllowed, type.getName(), name);
            Message msg = new Message(MSGCODE_WRONG_TYPE, text, Message.ERROR, this, PROPERTY_TEST_ATTRIBUTE_TYPE);
            messageList.add(msg);
        }
        
        // check if the type of the attribute matches the type of the parent
        TestParameterType parentType = ((ITestPolicyCmptTypeParameter)getParent()).getTestParameterType();
        if (!TestParameterType.isChildTypeMatching(type, parentType)) {
            String text = NLS.bind(Messages.TestAttribute_Error_TypeNotAllowedIfParent, type.getName(), parentType
                    .getName());
            Message msg = new Message(MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE, text, Message.ERROR, this,
                    PROPERTY_TEST_ATTRIBUTE_TYPE);
            messageList.add(msg);
        }
        
        // check for duplicate test attribute names and types
        TestPolicyCmptTypeParameter typeParam = (TestPolicyCmptTypeParameter)getParent();
        ITestAttribute testAttributes[] = typeParam.getTestAttributes();
        for (int i = 0; i < testAttributes.length; i++) {
            if (testAttributes[i] != this && testAttributes[i].getName().equals(name)) {
                String text = NLS.bind(Messages.TestAttribute_Error_DuplicateName, name);
                Message msg = new Message(MSGCODE_DUPLICATE_TEST_ATTRIBUTE_NAME, text, Message.ERROR, this,
                        PROPERTY_NAME);
                messageList.add(msg);
                break;
            } else if (testAttributes[i] != this && testAttributes[i].getAttribute().equals(attribute) && testAttributes[i].getTestAttributeType() == type){
                String text = NLS.bind(Messages.TestAttribute_ValidationError_DuplicateAttributeAndType, attribute, type.getName());
                Message msg = new Message(MSGCODE_DUPLICATE_ATTRIBUTE_AND_TYPE, text, Message.ERROR, this,
                        PROPERTY_ATTRIBUTE);
                messageList.add(msg);
                break;
            }
        }
        
        // check that derived (computed on the fly) arrtibutes are not supported
        if (modelAttribute != null){
            if (AttributeType.DERIVED_ON_THE_FLY.equals(modelAttribute.getAttributeType())){
                String text = Messages.TestAttribute_ValidationWarning_DerivedOnTheFlyAttributesAreNotSupported;
                Message msg = new Message(MSGCODE_DERIVED_ON_THE_FLY_ATTRIBUTES_NOT_SUPPORTED, text, Message.WARNING, this,
                        PROPERTY_TEST_ATTRIBUTE_TYPE);
                messageList.add(msg);
            }
        }
    }
}
