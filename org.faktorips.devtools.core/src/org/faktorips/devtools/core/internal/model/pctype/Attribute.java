/***************************************************************************************************
 *  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.  *  * Alle Rechte vorbehalten.  *  *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,  * Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der  * Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community)  * genutzt werden, die Bestandteil der Auslieferung ist und auch
 * unter  *   http://www.faktorips.org/legal/cl-v01.html  * eingesehen werden kann.  *  *
 * Mitwirkende:  *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  *  
 **************************************************************************************************/

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
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.internal.model.ValueSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IValueSet;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Implementation of IAttribute.
 */
public class Attribute extends IpsObjectPart implements IAttribute {

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

    private boolean deleted = false;

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
    public Attribute() {
        valueSet = new AllValuesValueSet(this, getNextPartId());
    }

    PolicyCmptType getPolicyCmptType() {
        return (PolicyCmptType)getIpsObject();
    }

    /**
     * {@inheritDoc}
     */
    public void delete() {
        getPolicyCmptType().removeAttribute(this);
        deleted = true;
        objectHasChanged();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * {@inheritDoc}
     */
    public void setName(String newName) {
        String oldName = name;
        this.name = newName;
        valueChanged(oldName, name);
    }

    /**
     * {@inheritDoc}
     */
    public String getDatatype() {
        if (!overwrites) {
            return datatype;
        }
        IAttribute superAttr;
        try {
            superAttr = findSupertypeAttribute();
            if (superAttr != null) {
                return superAttr.getDatatype();
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Returns the first attribute found with the same name in the supertypes hierarchy or
     * <code>null</code> if no such attribute exists.
     * 
     * @throws CoreException
     * 
     * @throws CoreException if an error occurs while searching the attribute.
     */
    private IAttribute findSupertypeAttribute() throws CoreException {
        IPolicyCmptType supertype = getPolicyCmptType().findSupertype();
        if (supertype == null) {
            return null;
        }
        // use the supertype to searchc because the findAttribute-Method of TypeHierarchy
        // searches the given type, too. So we can avoid to find this attribute.
        return supertype.getSupertypeHierarchy().findAttribute(supertype, name);
    }

    /**
     * {@inheritDoc}
     */
    public void setDatatype(String newDatatype) {
        String oldDatatype = datatype;
        this.datatype = newDatatype;
        valueChanged(oldDatatype, newDatatype);
    }

    /**
     * {@inheritDoc}
     */
    public ValueDatatype findDatatype() throws CoreException {
        return getIpsProject().findValueDatatype(getDatatype());
    }

    /**
     * {@inheritDoc}
     */
    public void setAttributeType(AttributeType newType) {
        AttributeType oldType = attributeType;
        attributeType = newType;
        valueChanged(oldType, newType);
    }

    /**
     * {@inheritDoc}
     */
    public IValidationRule findValueSetRule() {
        IValidationRule[] rules = getPolicyCmptType().getRules();
        
        for (int i = 0; i < rules.length; i++) {
            String[] attributes = rules[i].getValidatedAttributes();
            for (int j = 0; j < attributes.length; j++) {
                if (attributes[j].equals(getName()) && 
                    rules[i].isCheckValueAgainstValueSetRule()) {
                    return rules[i];
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IValidationRule createValueSetRule(){
        IValidationRule rule = findValueSetRule();
        if(rule != null){
            return rule;
        }
        rule = getPolicyCmptType().newRule();
        rule.addValidatedAttribute(getName());
        rule.setCheckValueAgainstValueSetRule(true);
        rule.setAppliedForAllBusinessFunctions(true);
        rule.setValidatedAttrSpecifiedInSrc(false);
        return rule;
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteValueSetRule(){
        IValidationRule rule = findValueSetRule();
        if(rule != null){
            rule.delete();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public AttributeType getAttributeType() {
        if (!overwrites) {
            return attributeType;
        }
        IAttribute superAttr;
        try {
            superAttr = findSupertypeAttribute();
            if (superAttr != null) {
                return superAttr.getAttributeType();
            }
            return AttributeType.CHANGEABLE;
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isChangeable() {
        return getAttributeType() == AttributeType.CHANGEABLE;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDerivedOrComputed() {
        return getAttributeType() == AttributeType.DERIVED || getAttributeType() == AttributeType.COMPUTED;
    }

    /**
     * {@inheritDoc}
     */
    public Modifier getModifier() {
        if (!overwrites) {
            return modifier;
        }
        IAttribute superAttr;
        try {
            superAttr = findSupertypeAttribute();
            if (superAttr != null) {
                return superAttr.getModifier();
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
        return Modifier.PUBLISHED;
    }

    /**
     * {@inheritDoc}
     */
    public void setModifier(Modifier newModifier) {
        Modifier oldModifier = modifier;
        modifier = newModifier;
        valueChanged(oldModifier, newModifier);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isProductRelevant() {
        if (!overwrites) {
            return productRelevant;
        }
        IAttribute superAttr;
        try {
            superAttr = findSupertypeAttribute();
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
        if (superAttr != null) {
            return superAttr.isProductRelevant();
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void setProductRelevant(boolean newValue) {
        boolean oldValue = productRelevant;
        productRelevant = newValue;
        valueChanged(oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    public void setDefaultValue(String newValue) {
        String oldValue = defaultValue;
        defaultValue = newValue;
        valueChanged(oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    public IValueSet getValueSet() {
        return valueSet;
    }

    /**
     * {@inheritDoc}
     */
    public void setValueSetType(ValueSetType type) {
        if (valueSet != null && type == valueSet.getValueSetType()) {
            return;
        }
        valueSet = type.newValueSet(this, getNextPartId());
        objectHasChanged();
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    public Image getImage() {
        String baseImageName = "AttributePublic.gif"; //$NON-NLS-1$
        if (isProductRelevant()) {
            return IpsPlugin.getDefault().getProductRelevantImage(baseImageName);
        } else {
        	return IpsPlugin.getDefault().getImage(baseImageName);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Parameter[] getFormulaParameters() {
        Parameter[] copy = new Parameter[parameters.length];
        System.arraycopy(parameters, 0, copy, 0, parameters.length);
        return copy;
    }

    /**
     * {@inheritDoc}
     */
    public int getNumOfFormulaParameters() {
        return parameters.length;
    }

    /**
     * {@inheritDoc}
     */
    public void setFormulaParameters(Parameter[] params) {
        parameters = new Parameter[params.length];
        System.arraycopy(params, 0, parameters, 0, params.length);
        objectHasChanged();
    }

    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList result) throws CoreException {
        super.validateThis(result);
        IStatus status = JavaConventions.validateFieldName(name);
        if (!status.isOK()) {
            result.add(new Message(MSGCODE_INVALID_ATTRIBUTE_NAME, Messages.Attribute_msgInvalidAttributeName + name
                    + "!", Message.ERROR, this, PROPERTY_NAME)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        ValueDatatype datatypeObject = ValidationUtils.checkValueDatatypeReference(getDatatype(), false, this,
                PROPERTY_DATATYPE, "", result); //$NON-NLS-1$
        if (datatypeObject != null) {
            validateDefaultValueAndValueSet(datatypeObject, result);
        } else {
            if (!StringUtils.isEmpty(defaultValue)) {
                String text = NLS.bind(Messages.Attribute_msgDefaultNotParsable_UnknownDatatype, defaultValue);
                result.add(new Message(MSGCODE_DEFAULT_NOT_PARSABLE_UNKNOWN_DATATYPE, text, Message.WARNING, this,
                        PROPERTY_DEFAULT_VALUE)); //$NON-NLS-1$
            }
        }

        if (isDerivedOrComputed() && isProductRelevant() && parameters.length == 0) {
            String text = Messages.Attribute_msgNoInputparams;
            result.add(new Message(MSGCODE_NO_INPUT_PARAMETERS, text, Message.WARNING, this)); //$NON-NLS-1$
        }
        for (int i = 0; i < parameters.length; i++) {
            validateParameter(parameters[i], result);
        }

        if (isProductRelevant() && !getPolicyCmptType().isConfigurableByProductCmptType()) {
            String text = Messages.Attribute_msgAttributeCantBeProductRelevantIfTypeIsNot;
            result.add(new Message(MSGCODE_ATTRIBUTE_CANT_BE_PRODUCT_RELEVANT_IF_TYPE_IS_NOT, text, Message.ERROR,
                    this, PROPERTY_PRODUCT_RELEVANT));
        }

        IAttribute[] allAttributes = getPolicyCmptType().getAttributes();
        for (int i = 0; i < allAttributes.length; i++) {
            if (allAttributes[i] != this && collideNames(allAttributes[i].getName(), name)) {
                String txt = Messages.Attribute_msgNameCollisionLocal;
                result.add(new Message(MSGCODE_NAME_COLLISION_LOCAL, txt, Message.ERROR, this, PROPERTY_NAME));
            }
        }

        IAttribute superAttr = findSupertypeAttribute();
        if (overwrites) {
            if (superAttr == null) {
                String text = NLS.bind(Messages.Attribute_msgNothingToOverwrite, getName());
                result.add(new Message(MSGCODE_NOTHING_TO_OVERWRITE, text, Message.ERROR, this, new String[] {
                        PROPERTY_OVERWRITES, PROPERTY_NAME }));
            } else {
                superAttr.getValueSet().containsValueSet(valueSet, result, valueSet, null);
            }
        } else {
            if (superAttr != null) {
                IPolicyCmptType type = (IPolicyCmptType)superAttr.getIpsObject();
                String text = NLS.bind(Messages.Attribute_msgNameCollision, type != null ? type.getQualifiedName()
                        : Messages.Attribute_msgpartUnknown, superAttr.getName());
                result.add(new Message(MSGCODE_NAME_COLLISION, text, Message.ERROR, this, new String[] {
                        PROPERTY_OVERWRITES, PROPERTY_NAME }));
            }
        }
    }

    private boolean collideNames(String name1, String name2) {
        if (name1.equals(name2)) {
            return true;
        }

        if (Math.min(name1.length(), name2.length()) > 1) {
            if ((name1.substring(1).equals(name2.substring(1)) && name1.substring(0, 1).equalsIgnoreCase(
                    name2.substring(0, 1)))) {
                return true;
            }
        }

        return false;

    }

    private void validateDefaultValueAndValueSet(ValueDatatype valueDatatype, MessageList result) throws CoreException {
        if (!valueDatatype.isParsable(defaultValue)) {
            String defaultValueInMsg = defaultValue;
            if (defaultValue == null) {
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
            if (defaultValue!=null && !valueSet.containsValue(defaultValue)) {
                result.add(new Message(MSGCODE_DEFAULT_NOT_IN_VALUESET, NLS.bind(
                        Messages.Attribute_msgDefaultNotInValueset, defaultValue), //$NON-NLS-1$
                        Message.WARNING, this, PROPERTY_DEFAULT_VALUE));
            }
        }
    }

    private void validateParameter(Parameter param, MessageList result) throws CoreException {
        if (!isDerivedOrComputed() && !isProductRelevant()) {
            String text = Messages.Attribute_msgNoParamsNeccessary;
            result.add(new Message(MSGCODE_NO_PARAMETERS_NECCESSARY, text, Message.WARNING, param)); //$NON-NLS-1$
        }
        if (StringUtils.isEmpty(param.getName())) {
            result.add(new Message(MSGCODE_EMPTY_PARAMETER_NAME, Messages.Attribute_msgEmptyName, Message.ERROR, param,
                    PROPERTY_FORMULAPARAM_NAME)); //$NON-NLS-1$
        } else {
            IStatus status = JavaConventions.validateIdentifier(param.getName());
            if (!status.isOK()) {
                result.add(new Message(MSGCODE_INVALID_PARAMETER_NAME, Messages.Attribute_msgInvalidParamName,
                        Message.ERROR, param, //$NON-NLS-1$
                        PROPERTY_FORMULAPARAM_NAME));
            }
        }
        if (StringUtils.isEmpty(param.getDatatype())) {
            result.add(new Message(MSGCODE_NO_DATATYPE_FOR_PARAMETER, Messages.Attribute_msgDatatypeEmpty,
                    Message.ERROR, param, PROPERTY_FORMULAPARAM_DATATYPE)); //$NON-NLS-1$
        } else {
            Datatype datatypeObject = getIpsProject().findDatatype(param.getDatatype());
            if (datatypeObject == null) {
                result.add(new Message(MSGCODE_DATATYPE_NOT_FOUND, NLS.bind(Messages.Attribute_msgDatatypeNotFound,
                        param.getDatatype()), //$NON-NLS-1$
                        Message.ERROR, param, PROPERTY_FORMULAPARAM_DATATYPE));
            } else {
                if (datatypeObject instanceof ValueDatatype) {
                    try {
                        result.add(datatypeObject.validate(),
                                new ObjectProperty(param, PROPERTY_FORMULAPARAM_DATATYPE), true);
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
        name = element.getAttribute("name"); //$NON-NLS-1$
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
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute("name", name); //$NON-NLS-1$
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
            valueSet = ValueSetType.newValueSet(xmlTag, this, id);
            return valueSet;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsElement[] getChildren() {
        if (valueSet != null) {
            return new IIpsElement[] { valueSet };
        } else {
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