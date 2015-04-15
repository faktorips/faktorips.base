/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.productcmpttype.ChangingOverTimePropertyValidator;
import org.faktorips.devtools.core.internal.model.type.Attribute;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.core.internal.model.valueset.ValueSet;
import org.faktorips.devtools.core.model.DatatypeUtil;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.AttributeProperty;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IAttribute.
 */
public class PolicyCmptTypeAttribute extends Attribute implements IPolicyCmptTypeAttribute {

    protected static final String TAG_NAME = "Attribute"; //$NON-NLS-1$

    private boolean productRelevant;

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
        initChangingOverTimeDefault();
    }

    private void initChangingOverTimeDefault() {
        try {
            IProductCmptType productCmptType = findProductCmptType(getIpsProject());
            if (productCmptType == null) {
                return;
            }
            setProperty(AttributeProperty.CHANGING_OVER_TIME, productCmptType.isChangingOverTime());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
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
        rule.setAppliedForAllBusinessFunctions(true);
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
    public IProductCmptTypeMethod findComputationMethod(IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(computationMethodSignature)) {
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
        return productRelevant;
    }

    @Override
    public void setProductRelevant(boolean newValue) {
        boolean oldValue = productRelevant;
        productRelevant = newValue;
        if (oldValue != newValue && !newValue) {
            computationMethodSignature = ""; //$NON-NLS-1$
        }
        valueChanged(oldValue, newValue, PROPERTY_PRODUCT_RELEVANT);
    }

    @Override
    public IValueSet getValueSet() {
        return valueSet;
    }

    @Override
    public List<ValueSetType> getAllowedValueSetTypes(IIpsProject ipsProject) throws CoreException {
        List<ValueSetType> types = ipsProject.getValueSetTypes(findDatatype(ipsProject));
        ValueDatatype datatype = findDatatype(ipsProject);
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
    protected void validateThis(MessageList result, IIpsProject ipsProject) throws CoreException {
        super.validateThis(result, ipsProject);
        validateProductRelevant(result, ipsProject);
        validateOverwrite(result, ipsProject);
        validateValueSetType(result);
        validateChangingOverTimeFlag(result);
    }

    private void validateProductRelevant(MessageList result, IIpsProject ipsProject) throws CoreException {
        if (isProductRelevant() && !getPolicyCmptType().isConfigurableByProductCmptType()) {
            String text = Messages.Attribute_msgAttributeCantBeProductRelevantIfTypeIsNot;
            result.add(new Message(MSGCODE_ATTRIBUTE_CANT_BE_PRODUCT_RELEVANT_IF_TYPE_IS_NOT, text, Message.ERROR,
                    this, PROPERTY_PRODUCT_RELEVANT));
        }
        if (isDerived() && isProductRelevant()) {
            if (StringUtils.isEmpty(computationMethodSignature)) {
                String text = NLS.bind(Messages.PolicyCmptTypeAttribute_msg_ComputationMethodSignatureIsMissing,
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
                        result.add(new Message(MSGCODE_COMPUTATION_MEHTOD_HAS_DIFFERENT_DATATYPE, text, Message.ERROR,
                                this, new String[] { PROPERTY_DATATYPE, PROPERTY_COMPUTATION_METHOD_SIGNATURE }));
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
        try {
            return getAllowedValueSetTypes(getIpsProject()).contains(valueSet.getValueSetType());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private void validateOverwrite(MessageList result, IIpsProject ipsProject) throws CoreException {
        if (isOverwrite()) {
            IPolicyCmptTypeAttribute superAttr = (IPolicyCmptTypeAttribute)findOverwrittenAttribute(ipsProject);
            if (superAttr != null) {
                if (!attributeType.equals(superAttr.getAttributeType())) {
                    // there is only one allowed change: superAttribute is derived on the fly and
                    // this attribute is changeable. See FIPS-1103
                    if (!(superAttr.getAttributeType() == AttributeType.DERIVED_ON_THE_FLY && attributeType == AttributeType.CHANGEABLE)) {
                        String text = Messages.PolicyCmptTypeAttribute_TypeOfOverwrittenAttributeCantBeChanged;
                        result.add(new Message(MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_TYPE, text, Message.ERROR,
                                this, new String[] { PROPERTY_ATTRIBUTE_TYPE }));
                    }
                }
            }
        }
    }

    private void validateValueSetType(MessageList result) {
        if (!isAllowedValueSet(getValueSet())) {
            String messageText = NLS.bind(
                    Messages.PolicyCmptTypeAttribute_msg_IllegalValueSetType,
                    getValueSet() == null ? StringUtils.EMPTY : org.faktorips.devtools.core.util.StringUtils
                            .quote(getValueSet().getValueSetType().getName()));
            result.add(new Message(MSGCODE_ILLEGAL_VALUESET_TYPE, messageText, Message.ERROR, this, PROPERTY_VALUE_SET));
        }
    }

    @Override
    protected void validateDefaultValue(ValueDatatype valueDatatype, MessageList result, IIpsProject ipsProject)
            throws CoreException {
        super.validateDefaultValue(valueDatatype, result, ipsProject);
        if (isDefaultValueForbidden(valueDatatype)) {
            expectNoDefaultValue(result);
        }
    }

    @Override
    protected void validateOverwrittenDatatype(IAttribute superAttr, MessageList result) {
        if (!getDatatype().equals(superAttr.getDatatype())) {
            result.add(new Message(MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_DATATYPE,
                    Messages.Attribute_msg_Overwritten_datatype_different, Message.ERROR, this, PROPERTY_DATATYPE));
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

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        properties = EnumSet.of(AttributeProperty.CHANGING_OVER_TIME);
        productRelevant = Boolean.valueOf(element.getAttribute(PROPERTY_PRODUCT_RELEVANT)).booleanValue();
        attributeType = AttributeType.getAttributeType(element.getAttribute(PROPERTY_ATTRIBUTE_TYPE));
        computationMethodSignature = element.getAttribute(PROPERTY_COMPUTATION_METHOD_SIGNATURE);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_PRODUCT_RELEVANT, "" + productRelevant); //$NON-NLS-1$
        element.setAttribute(PROPERTY_ATTRIBUTE_TYPE, attributeType.getId());
        element.setAttribute(PROPERTY_COMPUTATION_METHOD_SIGNATURE, "" + computationMethodSignature); //$NON-NLS-1$
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
        List<IIpsElement> children = new ArrayList<IIpsElement>(2);
        if (valueSet != null) {
            children.add(valueSet);
        }
        if (persistenceAttributeInfo != null) {
            children.add(persistenceAttributeInfo);
        }
        return children.toArray(new IIpsElement[children.size()]);
    }

    public ValueDatatype getValueDatatype() {
        try {
            // TODO v2 - signature getValueDatatype() is wrong
            Datatype type = findDatatype(getIpsProject());
            if (type != null) {
                return (ValueDatatype)type;
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
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
        return NLS.bind(Messages.Attribute_proposalForRuleName, StringUtils.capitalize(getName()));
    }

    @Override
    public String getProposalMsgCodeForValueSetRule() {
        return NLS.bind(Messages.Attribute_proposalForMsgCode, getName().toUpperCase());
    }

    @Override
    public String getPropertyName() {
        if (productRelevant) {
            return name;
        }
        return ""; //$NON-NLS-1$
    }

    @Override
    public ProductCmptPropertyType getProductCmptPropertyType() {
        return ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE;
    }

    @Override
    public IPersistentAttributeInfo getPersistenceAttributeInfo() {
        return persistenceAttributeInfo;
    }

    @Override
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException {
        return getPolicyCmptType().findProductCmptType(ipsProject);
    }

    @Override
    public boolean isPolicyCmptTypeProperty() {
        return true;
    }

    @Override
    public boolean isPropertyFor(IPropertyValue propertyValue) {
        return getProductCmptPropertyType().equals(propertyValue.getPropertyType())
                && getPropertyName().equals(propertyValue.getPropertyName());
    }
}
