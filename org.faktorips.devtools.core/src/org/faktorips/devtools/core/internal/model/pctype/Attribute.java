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
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.ValueSet;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
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
    private ValueSet valueSet = ValueSet.ALL_VALUES;

    /**
     * Creates a new attribute.
     * 
     * @param pcType The type the attribute belongs to.
     * @param id The attribute's unique id within the type.
     */
    Attribute(PolicyCmptType pcType, int id) {
        super(pcType, id);
    }

    /**
     * Constructor for testing purposes.
     */
    Attribute() {
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
        return datatype;
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
        return getIpsProject().findDatatype(datatype);
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
        return attributeType;
    }

    /**
     * Overridden method.
     */
    public boolean isChangeable() {
		return attributeType==AttributeType.CHANGEABLE;
	}
    
    /**
     * Overridden.
     */
	public boolean isDerivedOrComputed() {
        return attributeType==AttributeType.DERIVED || attributeType==AttributeType.COMPUTED;
    }

    /**
     * Overridden.
     */
    public Modifier getModifier() {
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
    public ValueSet getValueSet() {
        return valueSet;
    }

    /**
     * Overridden.
     */
    public void setValueSet(ValueSet valueSet) {
        this.valueSet = valueSet;
        updateSrcFile();
    }

    /**
     * Overridden.
     */
    public ConfigElementType getConfigElementType() {
        if (!productRelevant) {
            return null;
        }
        if (attributeType == AttributeType.CHANGEABLE) {
            return ConfigElementType.POLICY_ATTRIBUTE;
        }
        if (attributeType == AttributeType.CONSTANT) {
            return ConfigElementType.PRODUCT_ATTRIBUTE;
        }
        if (attributeType == AttributeType.COMPUTED || attributeType == AttributeType.DERIVED) {
            return ConfigElementType.FORMULA;
        }
        throw new RuntimeException("Unkown AttributeType!"); //$NON-NLS-1$
    }

    /**
     * Overridden.
     */
    public Image getImage() {
        if (modifier == Modifier.PRIVATE) {
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
            result.add(new Message("", Messages.Attribute_msgInvalidAttributeName + name + "!", Message.ERROR, this, PROPERTY_NAME)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        Datatype datatypeObject = ValidationUtils.checkDatatypeReference(datatype, true, false, this,
                PROPERTY_DATATYPE, result);
        if (datatypeObject == null) {
            if (!StringUtils.isEmpty(defaultValue)) {
                String text = NLS.bind(Messages.Attribute_msgDefaultNotParsable_UnknownDatatype, defaultValue);
                result.add(new Message("", text, Message.WARNING, this, PROPERTY_DEFAULT_VALUE)); //$NON-NLS-1$
            } else {
            }
        } else {
            if (!datatypeObject.isValueDatatype()) {
                if (!StringUtils.isEmpty(datatype)) {
                    String text = NLS.bind(Messages.Attribute_msgValueNotParsable_InvalidDatatype, defaultValue);
                    result.add(new Message("", text, Message.WARNING, this, PROPERTY_DEFAULT_VALUE)); //$NON-NLS-1$
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
                    String text = NLS.bind(Messages.Attribute_msgValueTypeMismatch, defaultValueInMsg, datatype);
                    result.add(new Message("", text, Message.ERROR, this, PROPERTY_DEFAULT_VALUE)); //$NON-NLS-1$
                    return;
                }
                if (valueSet != null) {
                    if (valueSet.containsValue(defaultValue, valueDatatype) == false) {
                        result.add(new Message("", NLS.bind(Messages.Attribute_msgDefaultNotInValueset, defaultValue), //$NON-NLS-1$
                                Message.ERROR, this,
                                PROPERTY_DEFAULT_VALUE));
                    }
                }
                valueSet.validate(valueDatatype, result);
            }
            if (isDerivedOrComputed() && isProductRelevant() && parameters.length == 0) {
                String text = Messages.Attribute_msgNoInputparams;
                result.add(new Message("", text, Message.WARNING, this)); //$NON-NLS-1$
            }
        }
        for (int i = 0; i < parameters.length; i++) {
            validate(parameters[i], result);
        }

    }

    private void validate(Parameter param, MessageList result) throws CoreException {
        if (!isDerivedOrComputed() && !isProductRelevant()) {
            String text = Messages.Attribute_msgNoParamsNeccessary;
            result.add(new Message("", text, Message.WARNING, param)); //$NON-NLS-1$
        }
        if (StringUtils.isEmpty(param.getName())) {
            result.add(new Message("", Messages.Attribute_msgEmptyName, Message.ERROR, param, PROPERTY_FORMULAPARAM_NAME)); //$NON-NLS-1$
        } else {
            IStatus status = JavaConventions.validateIdentifier(param.getName());
            if (!status.isOK()) {
                result
                        .add(new Message("", Messages.Attribute_msgInvalidParamName, Message.ERROR, param, //$NON-NLS-1$
                                PROPERTY_FORMULAPARAM_NAME));
            }
        }
        if (StringUtils.isEmpty(param.getDatatype())) {
            result.add(new Message("", Messages.Attribute_msgDatatypeEmpty, Message.ERROR, param, PROPERTY_FORMULAPARAM_DATATYPE)); //$NON-NLS-1$
        } else {
            Datatype datatypeObject = getIpsProject().findDatatype(param.getDatatype());
            if (datatypeObject == null) {
                result
                        .add(new Message("", NLS.bind(Messages.Attribute_msgDatatypeNotFound, param.getDatatype()), //$NON-NLS-1$
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
        datatype = element.getAttribute(PROPERTY_DATATYPE);
        modifier = Modifier.getModifier(element.getAttribute(PROPERTY_MODIFIER));
        attributeType = AttributeType.getAttributeType(element.getAttribute(PROPERTY_ATTRIBUTE_TYPE));
        productRelevant = Boolean.valueOf(element.getAttribute(PROPERTY_PRODUCT_RELEVANT)).booleanValue();
        defaultValue = ValueToXmlHelper.getValueFromElement(element, "DefaultValue"); //$NON-NLS-1$
        Element valueSetEl = XmlUtil.getFirstElement(element, ValueSet.XML_TAG);
        if (valueSetEl == null) {
            valueSet = ValueSet.ALL_VALUES;
        } else {
            valueSet = ValueSet.createFromXml(valueSetEl);
        }
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
        element.setAttribute(PROPERTY_DATATYPE, datatype);
        element.setAttribute(PROPERTY_PRODUCT_RELEVANT, "" + productRelevant); //$NON-NLS-1$
        element.setAttribute(PROPERTY_MODIFIER, modifier.getId());
        element.setAttribute(PROPERTY_ATTRIBUTE_TYPE, attributeType.getId());
        ValueToXmlHelper.addValueToElement(defaultValue, element, "DefaultValue"); //$NON-NLS-1$
        Document doc = element.getOwnerDocument();
        element.appendChild(valueSet.toXml(doc));
        for (int i = 0; i < parameters.length; i++) {
            Element newParamElement = doc.createElement(TAG_PROPERTY_PARAMETER);
            newParamElement.setAttribute(TAG_PARAM_NAME, parameters[i].getName());
            newParamElement.setAttribute(TAG_PARAM_DATATYPE, parameters[i].getDatatype());
            element.appendChild(newParamElement);
        }
    }
   
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}
}