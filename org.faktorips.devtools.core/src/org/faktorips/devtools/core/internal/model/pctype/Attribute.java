/***************************************************************************************************
 *  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.  *  * Alle Rechte vorbehalten.  *  *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,  * Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der  * Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community)  * genutzt werden, die Bestandteil der Auslieferung ist und auch
 * unter  *   http://www.faktorips.org/legal/cl-v01.html  * eingesehen werden kann.  *  *
 * Mitwirkende:  *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  *  
 **************************************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.AllValuesValueSet;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.internal.model.ValueSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IValueSet;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype2.ProdDefPropertyType;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IAttribute.
 */
public class Attribute extends IpsObjectPart implements IAttribute {

    final static String TAG_NAME = "Attribute"; //$NON-NLS-1$

    // member variables.
    private String datatype = ""; //$NON-NLS-1$
    private boolean productRelevant = false;
    private AttributeType attributeType = AttributeType.CHANGEABLE;
    private String defaultValue = null;
    private Modifier modifier = Modifier.PUBLISHED;
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
    public Attribute() {
        valueSet = new AllValuesValueSet(this, getNextPartId());
    }

    PolicyCmptType getPolicyCmptType() {
        return (PolicyCmptType)getIpsObject();
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
     * {@inheritDoc}
     */
    public IAttribute findSupertypeAttribute() throws CoreException {
        IPolicyCmptType supertype = getPolicyCmptType().findSupertype();
        if (supertype == null) {
            return null;
        }
        IAttribute a = supertype.findAttributeInSupertypeHierarchy(name);
        if (this==a) {
            return null; // can happen if the type hierarchy contains a cycle
        }
        return a;
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
        rule.setName(getProposalValueSetRuleName());
        rule.addValidatedAttribute(getName());
        rule.setMessageCode(getProposalMsgCodeForValueSetRule());
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
    public boolean isDerived() {
        return getAttributeType().isDerived();
    }
    
    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeMethod findMethodCalculationTheAttributesValues(IIpsProject ipsProject) throws CoreException {
        if (attributeType!=AttributeType.DERIVED_ON_THE_FLY) {
            return null;
        }
        IProductCmptType type = getPolicyCmptType().findProductCmptType(ipsProject);
        if (type==null) {
            return null;
        }
        return type.getFormulaSignature(name);
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
    public boolean isValueSetUpdateable() {
        return true;
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
        if (getAttributeType() == AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL || getAttributeType() == AttributeType.DERIVED_ON_THE_FLY) {
            return null;
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
    public String getComputationMethodName() {
        return "compute" + StringUtils.capitalise(getName());
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

    /**
     * {@inheritDoc}
     */
    protected void reinitPartCollections() {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void removePart(IIpsObjectPart part) {
        valueSet = new AllValuesValueSet(this, getNextPartId());
    }

    /**
     * {@inheritDoc}
     */
    public String getProposalValueSetRuleName() {
        return NLS.bind(Messages.Attribute_proposalForRuleName, StringUtils.capitalise(getName()));
    }

    /**
     * {@inheritDoc}
     */
    public String getProposalMsgCodeForValueSetRule() {
        return NLS.bind(Messages.Attribute_proposalForMsgCode, getName().toUpperCase());
    }
    
    
    /**
     * {@inheritDoc}
     * Implementation of IProdDefProperty.
     */
    public String getPropertyName() {
        if (productRelevant) {
            return name;
        }
        return "";
    }

    /**
     * {@inheritDoc}
     * Implementation of IProdDefProperty.
     */
    public ProdDefPropertyType getProdDefPropertyType() {
        return ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET;
    }

    /**
     * {@inheritDoc}
     * Implementation of IProdDefProperty.
     */
    public String getPropertyDatatype() {
        return getDatatype();
    }
}