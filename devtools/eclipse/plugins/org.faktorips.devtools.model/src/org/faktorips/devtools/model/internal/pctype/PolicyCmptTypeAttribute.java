/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.pctype;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.pctype.persistence.PersistentAttributeInfo;
import org.faktorips.devtools.model.internal.productcmpttype.ChangingOverTimePropertyValidator;
import org.faktorips.devtools.model.internal.type.Attribute;
import org.faktorips.devtools.model.internal.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.model.internal.valueset.ValueSet;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.AttributeType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.pctype.persistence.IPersistentAttributeInfo;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.type.AttributeProperty;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.devtools.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.model.util.DatatypeUtil;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of {@link IAttribute}.
 */
public class PolicyCmptTypeAttribute extends Attribute implements IPolicyCmptTypeAttribute {

    protected static final String TAG_NAME = "Attribute"; //$NON-NLS-1$

    private boolean valueSetConfiguredByProduct;

    private boolean relevanceConfiguredByProduct;

    private boolean genericValidationEnabled = getIpsProject() == null ? false
            : getIpsProject().getReadOnlyProperties().isGenericValidationDefaultEnabled();

    private AttributeType attributeType = AttributeType.CHANGEABLE;

    private IValueSet valueSet;

    private String computationMethodSignature = ""; //$NON-NLS-1$

    private IPersistentAttributeInfo persistenceAttributeInfo;

    /**
     * Creates a new attribute.
     *
     * @param pcType The type the attribute belongs to.
     * @param id The attribute's unique id within the type.
     */
    public PolicyCmptTypeAttribute(IPolicyCmptType pcType, String id) {
        super(pcType, id);
        valueSet = new UnrestrictedValueSet(this, getNextPartId());
        if (pcType.getIpsProject().isPersistenceSupportEnabled()) {
            persistenceAttributeInfo = newPart(PersistentAttributeInfo.class);
        }
    }

    @Override
    public IPolicyCmptType getPolicyCmptType() {
        return (IPolicyCmptType)getIpsObject();
    }

    @Override
    public void setAttributeType(AttributeType newType) {
        AttributeType oldType = attributeType;
        attributeType = newType;
        valueChanged(oldType, newType);
    }

    @Override
    public IValidationRule findValueSetRule(IIpsProject ipsProject) {
        List<IValidationRule> rules = getPolicyCmptType().getValidationRules();

        for (IValidationRule rule : rules) {
            String[] attributes = rule.getValidatedAttributes();
            for (String attribute : attributes) {
                if (attribute.equals(getName()) && rule.isCheckValueAgainstValueSetRule()) {
                    return rule;
                }
            }
        }
        return null;
    }

    @Override
    public IValidationRule createValueSetRule() {
        IValidationRule rule = findValueSetRule(getIpsProject());
        if (rule != null) {
            return rule;
        }
        rule = getPolicyCmptType().newRule();
        rule.setName(getProposalValueSetRuleName());
        rule.addValidatedAttribute(getName());
        rule.setMessageCode(getProposalMsgCodeForValueSetRule());
        rule.setCheckValueAgainstValueSetRule(true);
        rule.setValidatedAttrSpecifiedInSrc(false);
        return rule;
    }

    @Override
    public void deleteValueSetRule() {
        IValidationRule rule = findValueSetRule(getIpsProject());
        if (rule != null) {
            rule.delete();
        }
    }

    @Override
    public AttributeType getAttributeType() {
        return attributeType;
    }

    @Override
    public boolean isChangeable() {
        return getAttributeType() == AttributeType.CHANGEABLE;
    }

    @Override
    public String getPropertyDatatype() {
        return getDatatype();
    }

    @Override
    public boolean isDerived() {
        return getAttributeType().isDerived();
    }

    @Override
    public IProductCmptTypeMethod findComputationMethod(IIpsProject ipsProject) {
        if (IpsStringUtils.isEmpty(computationMethodSignature)) {
            return null;
        }
        IProductCmptType productCmptType = findProductCmptType(ipsProject);
        if (productCmptType == null) {
            return null;
        }
        return (IProductCmptTypeMethod)productCmptType.findMethod(computationMethodSignature, ipsProject);
    }

    @Override
    public boolean isProductRelevant() {
        return isValueSetConfiguredByProduct() || isRelevanceConfiguredByProduct();
    }

    @Override
    public boolean isValueSetConfiguredByProduct() {
        return valueSetConfiguredByProduct;
    }

    @Override
    public boolean isRelevanceConfiguredByProduct() {
        return relevanceConfiguredByProduct;
    }

    @Override
    public void setProductRelevant(boolean newValue) {
        setValueSetConfiguredByProduct(newValue);
    }

    private void updateProductRelevant(boolean oldProductRelevant) {
        boolean productRelevant = isProductRelevant();
        boolean noLongerProductRelevant = oldProductRelevant != productRelevant && !productRelevant;
        if (noLongerProductRelevant) {
            computationMethodSignature = ""; //$NON-NLS-1$
        }
        valueChanged(oldProductRelevant, valueSetConfiguredByProduct, PROPERTY_PRODUCT_RELEVANT);
    }

    @Override
    public void setValueSetConfiguredByProduct(boolean valueSetConfiguredByProduct) {
        boolean oldProductRelevant = isProductRelevant();
        boolean oldValue = this.valueSetConfiguredByProduct;
        this.valueSetConfiguredByProduct = valueSetConfiguredByProduct;
        valueChanged(oldValue, valueSetConfiguredByProduct, PROPERTY_VALUESET_CONFIGURED_BY_PRODUCT);
        updateProductRelevant(oldProductRelevant);
    }

    @Override
    public void setRelevanceConfiguredByProduct(boolean relevanceConfiguredByProduct) {
        boolean oldProductRelevant = isProductRelevant();
        boolean oldValue = this.relevanceConfiguredByProduct;
        this.relevanceConfiguredByProduct = relevanceConfiguredByProduct;
        valueChanged(oldValue, relevanceConfiguredByProduct, PROPERTY_RELEVANCE_CONFIGURED_BY_PRODUCT);
        updateProductRelevant(oldProductRelevant);
    }

    @Override
    public IValueSet getValueSet() {
        return valueSet;
    }

    @Override
    public List<ValueSetType> getAllowedValueSetTypes(IIpsProject ipsProject) {
        ValueDatatype datatype = findDatatype(ipsProject);
        List<ValueSetType> types = ipsProject.getValueSetTypes(datatype);
        if (isEnumValueSetIllegal(datatype)) {
            types.remove(ValueSetType.ENUM);
        }
        return types;
    }

    private boolean isEnumValueSetIllegal(ValueDatatype datatype) {
        return !isProductRelevant() && DatatypeUtil.isExtensibleEnumType(datatype);
    }

    @Override
    public void setValueSetType(ValueSetType type) {
        if (valueSet != null && type == valueSet.getValueSetType()) {
            return;
        }
        valueSet = type.newValueSet(this, getNextPartId());
        objectHasChanged();
    }

    @Override
    public IValueSet changeValueSetType(ValueSetType newType) {
        setValueSetType(newType);
        return valueSet;
    }

    @Override
    public boolean isValueSetUpdateable() {
        return true;
    }

    @Override
    public String getComputationMethodSignature() {
        return computationMethodSignature;
    }

    @Override
    public void setComputationMethodSignature(String newMethodName) {
        String oldName = computationMethodSignature;
        computationMethodSignature = newMethodName;
        valueChanged(oldName, newMethodName);
    }

    @Override
    protected void validateThis(MessageList result, IIpsProject ipsProject) {
        super.validateThis(result, ipsProject);
        validateProductRelevant(result, ipsProject);
        validateOverwrite(result, ipsProject);
        if (getValueDatatype() != null) {
            validateValueSetType(result);
        }
        validateChangingOverTimeFlag(result);
        validateAbstractDatatype(result);
    }

    private void validateProductRelevant(MessageList result, IIpsProject ipsProject) {
        if (isProductRelevant()) {
            if (!getPolicyCmptType().isConfigurableByProductCmptType()) {
                String text = Messages.Attribute_msgAttributeCantBeProductRelevantIfTypeIsNot;
                result.add(new Message(MSGCODE_ATTRIBUTE_CANT_BE_PRODUCT_RELEVANT_IF_TYPE_IS_NOT, text, Message.ERROR,
                        this, productRelevantProperties()));
            }
            if (isDerived()) {
                if (IpsStringUtils.isEmpty(computationMethodSignature)) {
                    String text = MessageFormat.format(
                            Messages.PolicyCmptTypeAttribute_msg_ComputationMethodSignatureIsMissing,
                            getName());
                    result.add(new Message(MSGCODE_COMPUTATION_METHOD_NOT_SPECIFIED, text, Message.ERROR, this,
                            PROPERTY_COMPUTATION_METHOD_SIGNATURE));
                } else {
                    IMethod computationMethod = findComputationMethod(ipsProject);
                    if (computationMethod == null) {
                        String text = Messages.PolicyCmptTypeAttribute_msg_ComputationMethodSignatureDoesNotExists;
                        result.add(new Message(MSGCODE_COMPUTATION_METHOD_DOES_NOT_EXIST, text, Message.ERROR, this,
                                PROPERTY_COMPUTATION_METHOD_SIGNATURE));
                    } else {
                        ValueDatatype attributeDataype = findDatatype(ipsProject);
                        if (attributeDataype != null
                                && !attributeDataype.equals(computationMethod.findDatatype(ipsProject))) {
                            String text = Messages.PolicyCmptTypeAttribute_msg_ComputationMethodSignatureHasADifferentDatatype;
                            result.add(new Message(MSGCODE_COMPUTATION_MEHTOD_HAS_DIFFERENT_DATATYPE, text,
                                    Message.ERROR, this, PROPERTY_DATATYPE, PROPERTY_COMPUTATION_METHOD_SIGNATURE));
                        }
                    }
                }
            }
        }
    }

    private void validateChangingOverTimeFlag(MessageList result) {
        if (!isProductRelevant() || !isChangeable()) {
            return;
        }
        ChangingOverTimePropertyValidator propertyValidator = new ChangingOverTimePropertyValidator(this);
        propertyValidator.validateTypeDoesNotAcceptChangingOverTime(result);
    }

    private boolean isAllowedValueSet(IValueSet valueSet) {
        if (valueSet == null) {
            return false;
        }
        return getAllowedValueSetTypes(getIpsProject()).contains(valueSet.getValueSetType());
    }

    private void validateOverwrite(MessageList result, IIpsProject ipsProject) {
        if (isOverwrite()) {
            IPolicyCmptTypeAttribute superAttr = (IPolicyCmptTypeAttribute)findOverwrittenAttribute(ipsProject);
            if (superAttr != null) {
                if (!attributeType.equals(superAttr.getAttributeType())) {
                    // there is only one allowed change: superAttribute is derived on the fly and
                    // this attribute is changeable. See FIPS-1103
                    if (!(superAttr.getAttributeType() == AttributeType.DERIVED_ON_THE_FLY
                            && attributeType == AttributeType.CHANGEABLE)) {
                        String text = Messages.PolicyCmptTypeAttribute_TypeOfOverwrittenAttributeCantBeChanged;
                        result.add(new Message(MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_TYPE, text, Message.ERROR,
                                this, PROPERTY_ATTRIBUTE_TYPE));
                    }
                }
                if (superAttr.isGenericValidationEnabled() && !isGenericValidationEnabled()) {
                    String text = Messages.PolicyCmptTypeAttribute_OverwrittenAttributeDisabledGenericValidation;
                    result.add(new Message(MSGCODE_OVERWRITTEN_ATTRIBUTE_DISABLED_GENERIC_VALIDATION, text,
                            Message.ERROR, this,
                            PROPERTY_GENERIC_VALIDATION));
                }
            }
        }
    }

    private void validateValueSetType(MessageList result) {
        if (!isAllowedValueSet(getValueSet())) {
            String messageText = MessageFormat.format(Messages.PolicyCmptTypeAttribute_msg_IllegalValueSetType,
                    getValueSet() == null ? IpsStringUtils.EMPTY
                            : org.faktorips.devtools.model.internal.util.StringUtils
                                    .quote(getValueSet().getValueSetType().getName()));
            result.add(
                    new Message(MSGCODE_ILLEGAL_VALUESET_TYPE, messageText, Message.ERROR, this, PROPERTY_VALUE_SET));
        }
    }

    @Override
    protected void validateDefaultValue(ValueDatatype valueDatatype, MessageList result, IIpsProject ipsProject) {
        super.validateDefaultValue(valueDatatype, result, ipsProject);
        if (isDefaultValueForbidden(valueDatatype)) {
            expectNoDefaultValue(result);
        }
    }

    private boolean isDefaultValueForbidden(ValueDatatype valueDatatype) {
        return DatatypeUtil.isExtensibleEnumType(valueDatatype) && !isProductRelevant();
    }

    private void expectNoDefaultValue(MessageList result) {
        if (getDefaultValue() != null) {
            result.newError(MSGCODE_DEFAULT_NOT_PARSABLE_INVALID_DATATYPE,
                    Messages.PolicyCmptTypeAttribute_msg_defaultValueExtensibleEnumType, this, PROPERTY_DEFAULT_VALUE);
        }
    }

    private void validateAbstractDatatype(MessageList result) {
        ValueDatatype datatype = findDatatype(getIpsProject());
        if (datatype != null && datatype.isAbstract()) {
            if (AttributeType.CONSTANT == getAttributeType()) {
                result.newError(MSGCODE_CONSTANT_CANT_BE_ABSTRACT,
                        Messages.PolicyCmptTypeAttribute_msg_ConstantCantBeAbstract, this, PROPERTY_ATTRIBUTE_TYPE,
                        PROPERTY_DATATYPE);
            }
            if (isProductRelevant()) {
                result.newError(MSGCODE_ABSTRACT_CANT_BE_PRODUCT_RELEVANT,
                        MessageFormat.format(Messages.PolicyCmptTypeAttribute_msg_AbstractCantBeProductRelevant,
                                getName(),
                                datatype.getName()),
                        this, productRelevantProperties(PROPERTY_DATATYPE));
            }
        }
    }

    @Override
    protected void validateAbstractDatatype(MessageList result, IIpsProject ipsProject) {
        if (isProductRelevant()) {
            super.validateAbstractDatatype(result, ipsProject);
        }
    }

    private String[] productRelevantProperties(String... otherProperties) {
        ArrayList<String> properties = new ArrayList<>();
        for (String property : otherProperties) {
            properties.add(property);
        }
        properties.add(PROPERTY_PRODUCT_RELEVANT);
        if (valueSetConfiguredByProduct) {
            properties.add(PROPERTY_VALUESET_CONFIGURED_BY_PRODUCT);
        }
        if (relevanceConfiguredByProduct) {
            properties.add(PROPERTY_RELEVANCE_CONFIGURED_BY_PRODUCT);
        }

        return properties.toArray(new String[0]);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        boolean productRelevant = ValueToXmlHelper.isAttributeTrue(element, PROPERTY_PRODUCT_RELEVANT);
        valueSetConfiguredByProduct = XmlUtil.getBooleanAttributeOrFalse(element,
                PROPERTY_VALUESET_CONFIGURED_BY_PRODUCT) || productRelevant;
        relevanceConfiguredByProduct = XmlUtil.getBooleanAttributeOrFalse(element,
                PROPERTY_RELEVANCE_CONFIGURED_BY_PRODUCT);
        attributeType = AttributeType.getAttributeType(element.getAttribute(PROPERTY_ATTRIBUTE_TYPE));
        computationMethodSignature = XmlUtil.getAttributeOrEmptyString(element, PROPERTY_COMPUTATION_METHOD_SIGNATURE);
        genericValidationEnabled = XmlUtil.getBooleanAttributeOrFalse(element, PROPERTY_GENERIC_VALIDATION);
    }

    @Override
    protected void initPropertyDefaultChangingOverTime() {
        IProductCmptType productCmptType = findProductCmptType(getIpsProject());
        boolean changingOverTime = productCmptType == null ? true : productCmptType.isChangingOverTime();
        setProperty(AttributeProperty.CHANGING_OVER_TIME, changingOverTime);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        if (valueSetConfiguredByProduct) {
            element.setAttribute(PROPERTY_VALUESET_CONFIGURED_BY_PRODUCT, "" + valueSetConfiguredByProduct); //$NON-NLS-1$
        }
        if (relevanceConfiguredByProduct) {
            element.setAttribute(PROPERTY_RELEVANCE_CONFIGURED_BY_PRODUCT, "" + relevanceConfiguredByProduct); //$NON-NLS-1$
        }
        element.setAttribute(PROPERTY_ATTRIBUTE_TYPE, attributeType.getId());
        if (IpsStringUtils.isNotEmpty(computationMethodSignature)) {
            element.setAttribute(PROPERTY_COMPUTATION_METHOD_SIGNATURE, "" + computationMethodSignature); //$NON-NLS-1$
        }
        if (genericValidationEnabled) {
            element.setAttribute(PROPERTY_GENERIC_VALIDATION, "" + genericValidationEnabled); //$NON-NLS-1$
        }
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        if (part instanceof IValueSet) {
            valueSet = (IValueSet)part;
            return true;

        } else if (part instanceof IPersistentAttributeInfo) {
            persistenceAttributeInfo = (IPersistentAttributeInfo)part;
            return true;
        }
        return false;
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        if (xmlTag.getNodeName().equals(ValueSet.XML_TAG)) {
            valueSet = ValueSetType.newValueSet(xmlTag, this, id);
            return valueSet;

        } else if (xmlTag.getTagName().equals(IPersistentAttributeInfo.XML_TAG)) {
            return newPersistentAttributeInfoInternal(id);
        }
        return null;
    }

    private IIpsObjectPart newPersistentAttributeInfoInternal(String id) {
        persistenceAttributeInfo = new PersistentAttributeInfo(this, id);
        return persistenceAttributeInfo;
    }

    @Override
    public IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        if (partType.isAssignableFrom(PersistentAttributeInfo.class)) {
            return new PersistentAttributeInfo(this, getNextPartId());
        } else {
            return null;
        }
    }

    @Override
    protected void reinitPartCollectionsThis() {
        // TODO Joerg Merge PersistenceBranch: wirklich nicht valueSet neu anlegen?
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        if (part == valueSet) {
            valueSet = new UnrestrictedValueSet(this, getNextPartId());
            return true;
        } else if (part == persistenceAttributeInfo) {
            persistenceAttributeInfo = newPart(PersistentAttributeInfo.class);
        }
        return false;
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        List<IIpsElement> children = new ArrayList<>(2);
        if (valueSet != null) {
            children.add(valueSet);
        }
        if (persistenceAttributeInfo != null) {
            children.add(persistenceAttributeInfo);
        }
        return children.toArray(new IIpsElement[children.size()]);
    }

    public ValueDatatype getValueDatatype() {
        // TODO v2 - signature getValueDatatype() is wrong
        Datatype type = findDatatype(getIpsProject());
        if (type != null) {
            return (ValueDatatype)type;
        }
        return null;
    }

    @Override
    public void setValueSetCopy(IValueSet source) {
        IValueSet oldset = valueSet;
        valueSet = source.copy(this, getNextPartId());
        valueChanged(oldset, valueSet);
    }

    @Override
    public String getProposalValueSetRuleName() {
        return getProposalValueSetRuleName(getName());
    }

    public static String getProposalValueSetRuleName(String attributeName) {
        return MessageFormat.format(Messages.Attribute_proposalForRuleName, StringUtils.capitalize(attributeName));
    }

    @Override
    public String getProposalMsgCodeForValueSetRule() {
        return getProposalMsgCodeForValueSetRule(getName());
    }

    public static String getProposalMsgCodeForValueSetRule(String attributeName) {
        return MessageFormat.format(Messages.Attribute_proposalForMsgCode, attributeName.toUpperCase());
    }

    @Override
    public String getPropertyName() {
        if (isProductRelevant()) {
            return name;
        }
        return ""; //$NON-NLS-1$
    }

    @Override
    public ProductCmptPropertyType getProductCmptPropertyType() {
        return ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE;
    }

    @Override
    public List<PropertyValueType> getPropertyValueTypes() {
        return Arrays.asList(PropertyValueType.CONFIGURED_VALUESET, PropertyValueType.CONFIGURED_DEFAULT);
    }

    @Override
    public IPersistentAttributeInfo getPersistenceAttributeInfo() {
        return persistenceAttributeInfo;
    }

    @Override
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) {
        return getPolicyCmptType().findProductCmptType(ipsProject);
    }

    @Override
    public boolean isPolicyCmptTypeProperty() {
        return true;
    }

    @Override
    public boolean isPropertyFor(IPropertyValue propertyValue) {
        return getProductCmptPropertyType().isMatchingPropertyValue(getPropertyName(), propertyValue);
    }

    @Override
    public boolean isChangingOverTimeValidationNecessary() {
        return isProductRelevant() && getAttributeType().equals(AttributeType.CHANGEABLE);
    }

    @Override
    public boolean isGenericValidationEnabled() {
        return genericValidationEnabled;
    }

    @Override
    public void setGenericValidationEnabled(boolean genericValidationEnabled) {
        boolean oldGenericValidationEnabled = isGenericValidationEnabled();
        this.genericValidationEnabled = genericValidationEnabled;
        valueChanged(oldGenericValidationEnabled, genericValidationEnabled);
    }

}
