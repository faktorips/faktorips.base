/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.AllValuesValueSet;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.internal.model.ValueSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IValueSet;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.XmlUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Implementation of IAttribute.
 */
public class Attribute extends Member implements IAttribute {

    private static final String TAG_PROPERTY_PARAMETER = "FormulaParameter"; //$NON-NLS-1$
    final static String TAG_NAME = "Attribute"; //$NON-NLS-1$
    private static final String TAG_PARAM_NAME = "name"; //$NON-NLS-1$
    private static final String TAG_PARAM_DATATYPE = "datatype"; //$NON-NLS-1$

    // member variables.
    private String datatype = ""; //$NON-NLS-1$
    private boolean productRelevant = true;
    private AttributeType attributeType = AttributeType.CHANGEABLE;
    private String defaultValue = null;
    private Modifier modifier = Modifier.PUBLISHED;
    private Parameter[] parameters = new Parameter[0];
    private IValueSet valueSet;
    private boolean overwrites = false;

    /**
     * Creates a new attribute.
     * 
     * @param pcType The type the attribute belongs to.
     * @param id The attribute's unique id within the type.
     */
    Attribute(PolicyCmptType pcType, int id) {
        super(pcType, id);
        valueSet = new AllValuesValueSet(this, getNextPartId());
    }

    /**
     * Constructor for testing purposes.
     */
    Attribute() {
        valueSet = new AllValuesValueSet(this, getNextPartId());
    }

    PolicyCmptType getPolicyCmptType() {
        return (PolicyCmptType)getIpsObject();
    }

    /**
     * Overridden.
     */
    public void delete() {
        getPolicyCmptType().removeAttribute(this);
        updateSrcFile();
        deleted = true;
    }

    private boolean deleted = false;

    /**
     * {@inheritDoc}
     */
    public boolean isDeleted() {
    	return deleted;
    }
    
    /**
     * Overridden.
     */
    public String getDatatype() {
    	if (overwrites) {
    		Attribute superAttr = getSupertypeAttribute();
    		if (superAttr != null) {
    			return superAttr.getDatatype();
    		}
    		return "";
    	}
        return datatype;
    }
    
    /**
     * Returns the first found attribute with the same name in supertypes or <code>null</code>
     * if no such attribute exists.
     */
    private Attribute getSupertypeAttribute() {
		IPolicyCmptType type = this.getPolicyCmptType();

		if (type == null) {
			return null;
		}
		
		try {
			// get the supertype because the findAttribute-Method of TypeHierarchy 
			// searches the given type, too. So we can avoid to find this attribute.
			type = type.findSupertype();
			if (type == null) {
				return null;
			}

			Attribute attr = (Attribute)type.getSupertypeHierarchy().findAttribute(type, name); 
			
			return attr;
		} catch (CoreException e) {
			IpsPlugin.log(e);
			return null;
		}
    }

    /**
     * Overridden.
     */
    public void setDatatype(String newDatatype) {
        String oldDatatype = datatype;
        this.datatype = newDatatype;
        valueChanged(oldDatatype, newDatatype);
    }
    
    /**
     * Overridden.
     */
    public Datatype findDatatype() throws CoreException {
        return getIpsProject().findDatatype(getDatatype());
    }

    /**
     * Overridden.
     */
    public void setAttributeType(AttributeType newType) {
        AttributeType oldType = attributeType;
        attributeType = newType;
        valueChanged(oldType, newType);
    }

    /**
     * Overridden.
     */
    public AttributeType getAttributeType() {
    	if (overwrites) {
    		Attribute superAttr = getSupertypeAttribute();
    		if (superAttr != null) {
    			return superAttr.getAttributeType();
    		}
    		return AttributeType.CHANGEABLE;
    	}
        return attributeType;
    }

    /**
     * Overridden method.
     */
    public boolean isChangeable() {
		return getAttributeType()==AttributeType.CHANGEABLE;
	}
    
    /**
     * Overridden.
     */
	public boolean isDerivedOrComputed() {
        return getAttributeType()==AttributeType.DERIVED || getAttributeType()==AttributeType.COMPUTED;
    }

    /**
     * Overridden.
     */
    public Modifier getModifier() {
    	if (overwrites) {
    		Attribute superAttr = getSupertypeAttribute();
    		if (superAttr != null) {
    			return superAttr.getModifier();
    		}
    		return Modifier.PUBLISHED;
    	}
        return modifier;
    }

    /**
     * Overridden.
     */
    public void setModifier(Modifier newModifier) {
        Modifier oldModifier = modifier;
        modifier = newModifier;
        valueChanged(oldModifier, newModifier);
    }

    /**
     * Overridden.
     */
    public boolean isProductRelevant() {
    	if (overwrites) {
    		Attribute superAttr = getSupertypeAttribute();
    		if (superAttr != null) {
    			return superAttr.isProductRelevant();
    		}
    		return true;
    	}
        return productRelevant;
    }

    /**
     * Overridden.
     */
    public void setProductRelevant(boolean newValue) {
        boolean oldValue = productRelevant;
        productRelevant = newValue;
        valueChanged(oldValue, newValue);
    }

    /**
     * Overridden.
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Overridden.
     */
    public void setDefaultValue(String newValue) {
        String oldValue = defaultValue;
        defaultValue = newValue;
        valueChanged(oldValue, newValue);
    }

    /**
     * Overridden.
     */
    public IValueSet getValueSet() {
        return valueSet;
    }

    /**
     * Overridden.
     */
    public void setValueSetType(ValueSetType type) {
    	if (valueSet != null && type == valueSet.getValueSetType()) {
    		return;
    	}
    	valueSet = type.newValueSet(this, getNextPartId());
    	updateSrcFile();
    }

    /**
     * Overridden.
     */
    public ConfigElementType getConfigElementType() {
        if (!isProductRelevant()) {
            return null;
        }
        if (getAttributeType() == AttributeType.CHANGEABLE) {
            return ConfigElementType.POLICY_ATTRIBUTE;
        }
        if (getAttributeType() == AttributeType.CONSTANT) {
            return ConfigElementType.PRODUCT_ATTRIBUTE;
        }
        if (getAttributeType() == AttributeType.COMPUTED || getAttributeType() == AttributeType.DERIVED) {
            return ConfigElementType.FORMULA;
        }
        throw new RuntimeException("Unkown AttributeType!"); //$NON-NLS-1$
    }

    /**
     * Overridden.
     */
    public Image getImage() {
        if (getModifier() == Modifier.PRIVATE) {
            return IpsPlugin.getDefault().getImage("AttributePrivate.gif"); //$NON-NLS-1$
        } else {
            return IpsPlugin.getDefault().getImage("AttributePublic.gif"); //$NON-NLS-1$
        }
    }

    /**
     * Overridden.
     */
    public Parameter[] getFormulaParameters() {
        Parameter[] copy = new Parameter[parameters.length];
        System.arraycopy(parameters, 0, copy, 0, parameters.length);
        return copy;
    }

    /**
     * Overridden.
     */
    public int getNumOfFormulaParameters() {
        return parameters.length;
    }

    /**
     * Overridden.
     */
    public void setFormulaParameters(Parameter[] params) {
        parameters = new Parameter[params.length];
        System.arraycopy(params, 0, parameters, 0, params.length);
        updateSrcFile();
    }

    /**
     * Overridden.
     */
    public void validate(MessageList result) throws CoreException {
    	super.validate(result);
        IStatus status = JavaConventions.validateFieldName(name);
        if (!status.isOK()) {
            result.add(new Message(MSGCODE_INVALID_ATTRIBUTE_NAME, Messages.Attribute_msgInvalidAttributeName + name + "!", Message.ERROR, this, PROPERTY_NAME)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        Datatype datatypeObject = ValidationUtils.checkDatatypeReference(getDatatype(), false, this,
                PROPERTY_DATATYPE, "", result); //$NON-NLS-1$
        if (datatypeObject == null) {
            if (!StringUtils.isEmpty(defaultValue)) {
                String text = NLS.bind(Messages.Attribute_msgDefaultNotParsable_UnknownDatatype, defaultValue);
                result.add(new Message(MSGCODE_DEFAULT_NOT_PARSABLE_UNKNOWN_DATATYPE, text, Message.WARNING, this, PROPERTY_DEFAULT_VALUE)); //$NON-NLS-1$
            } else {
            }
        } else {
            if (!datatypeObject.isValueDatatype()) {
                if (!StringUtils.isEmpty(getDatatype())) {
                    String text = NLS.bind(Messages.Attribute_msgValueNotParsable_InvalidDatatype, defaultValue);
                    result.add(new Message(MSGCODE_DEFAULT_NOT_PARSABLE_INVALID_DATATYPE, text, Message.WARNING, this, PROPERTY_DEFAULT_VALUE)); //$NON-NLS-1$
                } else {
                }
            } else {
                ValueDatatype valueDatatype = (ValueDatatype)datatypeObject;
                if (!valueDatatype.isParsable(defaultValue)) {
                	String defaultValueInMsg = defaultValue;
                	if (defaultValue==null) {
                		defaultValueInMsg = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
                	} else if (defaultValue.equals("")) { //$NON-NLS-1$
                		defaultValueInMsg = Messages.Attribute_msgDefaultValueIsEmptyString;
                	}
                    String text = NLS.bind(Messages.Attribute_msgValueTypeMismatch, defaultValueInMsg, getDatatype());
                    result.add(new Message(MSGCODE_VALUE_NOT_PARSABLE, text, Message.ERROR, this, PROPERTY_DEFAULT_VALUE)); //$NON-NLS-1$
                    return;
                }
                if (valueSet != null) {
                    valueSet.validate(result);

                    if (valueSet.containsValue(defaultValue) == false) {
                        result.add(new Message(MSGCODE_DEFAULT_NOT_IN_VALUESET, NLS.bind(Messages.Attribute_msgDefaultNotInValueset, defaultValue), //$NON-NLS-1$
                                Message.ERROR, this,
                                PROPERTY_DEFAULT_VALUE));
                    }
                    
                    if (overwrites) {
                    	getSupertypeAttribute().getValueSet().containsValueSet(valueSet, result, valueSet, null);
                    }
                }
            }
        }
        
        if (isDerivedOrComputed() && isProductRelevant() && parameters.length == 0) {
            String text = Messages.Attribute_msgNoInputparams;
            result.add(new Message(MSGCODE_NO_INPUT_PARAMETERS, text, Message.WARNING, this)); //$NON-NLS-1$
        }

        for (int i = 0; i < parameters.length; i++) {
            validate(parameters[i], result);
        }

        if (isProductRelevant() && !getPolicyCmptType().isConfigurableByProductCmptType()) {
        	String text = Messages.Attribute_msgAttributeCantBeProductRelevantIfTypeIsNot;
        	result.add(new Message(MSGCODE_ATTRIBUTE_CANT_BE_PRODUCT_RELEVANT_IF_TYPE_IS_NOT, text, Message.ERROR, this, PROPERTY_PRODUCT_RELEVANT));
        }
        
        Attribute superAttr = getSupertypeAttribute();
        if (!overwrites && superAttr != null) {
        	IPolicyCmptType type = superAttr.getPolicyCmptType();
        	String text = NLS.bind("Name collision with {0}:{1}", type!=null?type.getQualifiedName():"unknown", superAttr.getName());
        	result.add(new Message(MSGCODE_NAME_COLLISION, text, Message.ERROR, this, new String[] {PROPERTY_OVERWRITES, PROPERTY_NAME}));
        }
        
        if (overwrites && superAttr == null) {
        	String text = NLS.bind("No attribute {0} in supertype hierarchy, so nothing can be overwritten.", getName());
        	result.add(new Message(MSGCODE_NOTHING_TO_OVERWRITE, text, Message.ERROR, this, new String[] {PROPERTY_OVERWRITES, PROPERTY_NAME}));
        }
    }

    private void validate(Parameter param, MessageList result) throws CoreException {
        if (!isDerivedOrComputed() && !isProductRelevant()) {
            String text = Messages.Attribute_msgNoParamsNeccessary;
            result.add(new Message(MSGCODE_NO_PARAMETERS_NECCESSARY, text, Message.WARNING, param)); //$NON-NLS-1$
        }
        if (StringUtils.isEmpty(param.getName())) {
            result.add(new Message(MSGCODE_EMPTY_PARAMETER_NAME, Messages.Attribute_msgEmptyName, Message.ERROR, param, PROPERTY_FORMULAPARAM_NAME)); //$NON-NLS-1$
        } else {
            IStatus status = JavaConventions.validateIdentifier(param.getName());
            if (!status.isOK()) {
                result.add(new Message(MSGCODE_INVALID_PARAMETER_NAME, Messages.Attribute_msgInvalidParamName, Message.ERROR, param, //$NON-NLS-1$
                                PROPERTY_FORMULAPARAM_NAME));
            }
        }
        if (StringUtils.isEmpty(param.getDatatype())) {
            result.add(new Message(MSGCODE_NO_DATATYPE_FOR_PARAMETER, Messages.Attribute_msgDatatypeEmpty, Message.ERROR, param, PROPERTY_FORMULAPARAM_DATATYPE)); //$NON-NLS-1$
        } else {
            Datatype datatypeObject = getIpsProject().findDatatype(param.getDatatype());
            if (datatypeObject == null) {
                result.add(new Message(MSGCODE_DATATYPE_NOT_FOUND, NLS.bind(Messages.Attribute_msgDatatypeNotFound, param.getDatatype()), //$NON-NLS-1$
                                Message.ERROR, param,
                                PROPERTY_FORMULAPARAM_DATATYPE));
            } else {
            	if (datatypeObject instanceof ValueDatatype) {
                    try {
                        result.add(datatypeObject.validate(), new ObjectProperty(param, PROPERTY_FORMULAPARAM_DATATYPE), true);
                    } catch (CoreException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new CoreException(new IpsStatus(e));
                    }
            	}
            }
        }
    }

    /**
     * Overridden.
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }
   
    /**
     * Overridden.
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        
        overwrites = Boolean.valueOf(element.getAttribute(PROPERTY_OVERWRITES)).booleanValue();

        if (!overwrites) {
        	// these values are only neccessary if this attribute does not overwrite one.
        	datatype = element.getAttribute(PROPERTY_DATATYPE);
        	modifier = Modifier.getModifier(element.getAttribute(PROPERTY_MODIFIER));
        	attributeType = AttributeType.getAttributeType(element.getAttribute(PROPERTY_ATTRIBUTE_TYPE));
        	productRelevant = Boolean.valueOf(element.getAttribute(PROPERTY_PRODUCT_RELEVANT)).booleanValue();
        }
        defaultValue = ValueToXmlHelper.getValueFromElement(element, "DefaultValue"); //$NON-NLS-1$

        // get the nodes with the parameter information
        NodeList nl = element.getElementsByTagName(TAG_PROPERTY_PARAMETER);
        List params = new ArrayList();
        int paramIndex = 0;
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element paramElement = (Element)nl.item(i);
                Parameter newParam = new Parameter(paramIndex);
                newParam.setName(paramElement.getAttribute(TAG_PARAM_NAME));
                newParam.setDatatype(paramElement.getAttribute(TAG_PARAM_DATATYPE));
                params.add(newParam);
                paramIndex++;
            }
        }
        parameters = (Parameter[])params.toArray(new Parameter[0]);
    }
    
    /**
     * Overridden.
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        
        element.setAttribute(PROPERTY_OVERWRITES, "" + overwrites); //$NON-NLS-1$
        
        if (!overwrites) {
        	// these values are only neccessary if this attribute does not overwrite one.
        	element.setAttribute(PROPERTY_DATATYPE, datatype);
        	element.setAttribute(PROPERTY_PRODUCT_RELEVANT, "" + productRelevant); //$NON-NLS-1$
        	element.setAttribute(PROPERTY_MODIFIER, modifier.getId());
        	element.setAttribute(PROPERTY_ATTRIBUTE_TYPE, attributeType.getId());
        }
        ValueToXmlHelper.addValueToElement(defaultValue, element, "DefaultValue"); //$NON-NLS-1$
        Document doc = element.getOwnerDocument();
        for (int i = 0; i < parameters.length; i++) {
            Element newParamElement = doc.createElement(TAG_PROPERTY_PARAMETER);
            newParamElement.setAttribute(TAG_PARAM_NAME, parameters[i].getName());
            newParamElement.setAttribute(TAG_PARAM_DATATYPE, parameters[i].getDatatype());
            element.appendChild(newParamElement);
        }
    }
   
    /**
     * {@inheritDoc}
     */
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}

	/**
     * {@inheritDoc}
     */
    protected void reAddPart(IIpsObjectPart part) {
    	valueSet = (IValueSet)part;
    }
    
	/**
	 * {@inheritDoc}
	 */
    protected IIpsObjectPart newPart(Element xmlTag, int id) {
    	if (xmlTag.getNodeName().equals(ValueSet.XML_TAG)) {
    		Element valueSetNode = XmlUtil.getFirstElement(xmlTag);
    		valueSet = ValueSetType.newValueSet(valueSetNode, this, id);
    		return valueSet;
    	}
        return null;
    }

	/**
	 * {@inheritDoc}
	 */
	public IIpsElement[] getChildren() {
		if (valueSet != null) {
			return new IIpsElement[] {valueSet};
		}
		else {
			return new IIpsElement[0];
		}
    }

	/**
	 * {@inheritDoc}
	 */
	public ValueDatatype getValueDatatype() {
		try {
			Datatype type = findDatatype();
			if (type instanceof ValueDatatype) {
				return (ValueDatatype)type;
			}
		} catch (CoreException e) {
			IpsPlugin.log(e);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getOverwrites() {
		return overwrites;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOverwrites(boolean overwrites) {
		boolean old = this.overwrites;
		this.overwrites = overwrites;
		valueChanged(old, overwrites);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValueSetCopy(IValueSet source) {
		IValueSet oldset = valueSet;
		valueSet = source.copy(this, getNextPartId());
		valueChanged(oldset, valueSet);
	}
}