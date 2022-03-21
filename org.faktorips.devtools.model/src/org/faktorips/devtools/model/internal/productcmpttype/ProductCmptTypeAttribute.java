/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpttype;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.productcmpt.MultiValueHolder;
import org.faktorips.devtools.model.internal.type.Attribute;
import org.faktorips.devtools.model.internal.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.model.internal.valueset.ValueSet;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.type.AttributeProperty;
import org.faktorips.devtools.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IAttribute.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeAttribute extends Attribute implements IProductCmptTypeAttribute {

    static final String TAG_NAME = "Attribute"; //$NON-NLS-1$

    private IValueSet valueSet;

    public ProductCmptTypeAttribute(IProductCmptType parent, String id) {
        super(parent, id);
        valueSet = new UnrestrictedValueSet(this, getNextPartId());
        initPropertyDefaultVisible();
    }

    private void initPropertyDefaultVisible() {
        setProperty(AttributeProperty.VISIBLE, true);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        initPropertyDefaultVisible();
        if (element.hasAttribute(PROPERTY_MULTI_VALUE_ATTRIBUTE)) {
            String multiValueAttributeElement = element.getAttribute(PROPERTY_MULTI_VALUE_ATTRIBUTE);
            setProperty(AttributeProperty.MULTI_VALUE_ATTRIBUTE, Boolean.parseBoolean(multiValueAttributeElement));
        }
        if (element.hasAttribute(PROPERTY_VISIBLE)) {
            String visibleElement = element.getAttribute(PROPERTY_VISIBLE);
            setProperty(AttributeProperty.VISIBLE, Boolean.parseBoolean(visibleElement));
        }
        if (element.hasAttribute(PROPERTY_MULTILINGUAL)) {
            String multiLanguageAttribute = element.getAttribute(PROPERTY_MULTILINGUAL);
            setProperty(AttributeProperty.MULTILINGUAL, Boolean.parseBoolean(multiLanguageAttribute));
        }
    }

    @Override
    protected void initPropertyDefaultChangingOverTime() {
        setProperty(AttributeProperty.CHANGING_OVER_TIME, getProductCmptType().isChangingOverTime());
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_MULTI_VALUE_ATTRIBUTE, String.valueOf(isMultiValueAttribute()));
        element.setAttribute(PROPERTY_VISIBLE, String.valueOf(isVisible()));
        element.setAttribute(PROPERTY_MULTILINGUAL, String.valueOf(isMultilingual()));
    }

    @Override
    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }

    @Override
    public String getPropertyName() {
        return getName();
    }

    @Override
    public ProductCmptPropertyType getProductCmptPropertyType() {
        return ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE;
    }

    @Override
    public List<PropertyValueType> getPropertyValueTypes() {
        return Arrays.asList(PropertyValueType.ATTRIBUTE_VALUE);
    }

    @Override
    public boolean isDerived() {
        return false;
    }

    public ValueDatatype getValueDatatype() {
        return findDatatype(getIpsProject());
    }

    @Override
    public IValueSet getValueSet() {
        return valueSet;
    }

    @Override
    public List<ValueSetType> getAllowedValueSetTypes(IIpsProject ipsProject) {
        if (isMultilingual()) {
            ArrayList<ValueSetType> types = new ArrayList<>();
            types.add(ValueSetType.UNRESTRICTED);
            return types;
        } else {
            return ipsProject.getValueSetTypes(findDatatype(ipsProject));
        }
    }

    @Override
    public boolean isValueSetUpdateable() {
        return true;
    }

    @Override
    public void setValueSetType(ValueSetType newType) {
        ArgumentCheck.notNull(newType);
        if (newType == valueSet.getValueSetType()) {
            return;
        }
        valueSet = newType.newValueSet(this, getNextPartId());
        objectHasChanged();
    }

    @Override
    public IValueSet changeValueSetType(ValueSetType newType) {
        setValueSetType(newType);
        return valueSet;
    }

    @Override
    public void setValueSetCopy(IValueSet source) {
        IValueSet oldset = valueSet;
        valueSet = source.copy(this, getNextPartId());
        valueChanged(oldset, valueSet);
    }

    @Override
    public boolean isMultiValueAttribute() {
        return isPropertySet(AttributeProperty.MULTI_VALUE_ATTRIBUTE);
    }

    @Override
    public void setMultiValueAttribute(boolean multiValueAttribute) {
        boolean oldValue = isPropertySet(AttributeProperty.MULTI_VALUE_ATTRIBUTE);
        setProperty(AttributeProperty.MULTI_VALUE_ATTRIBUTE, multiValueAttribute);
        valueChanged(oldValue, multiValueAttribute);
    }

    @Override
    public boolean isVisible() {
        return isPropertySet(AttributeProperty.VISIBLE);
    }

    @Override
    public void setVisible(boolean visible) {
        boolean old = isPropertySet(AttributeProperty.VISIBLE);
        setProperty(AttributeProperty.VISIBLE, visible);
        valueChanged(old, visible, PROPERTY_VISIBLE);
    }

    @Override
    public boolean isMultilingualSupported() {
        return Datatype.STRING.getQualifiedName().equals(getDatatype());
    }

    @Override
    public boolean isMultilingual() {
        return isPropertySet(AttributeProperty.MULTILINGUAL) && isMultilingualSupported();
    }

    @Override
    public void setMultilingual(boolean multilingual) {
        boolean old = isPropertySet(AttributeProperty.MULTILINGUAL);
        setProperty(AttributeProperty.MULTILINGUAL, multilingual);
        valueChanged(old, multilingual, PROPERTY_MULTILINGUAL);
    }

    @Override
    public String getPropertyDatatype() {
        return getDatatype();
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        if (valueSet != null) {
            return new IIpsElement[] { valueSet };
        }
        return new IIpsElement[0];
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        if (part instanceof IValueSet) {
            valueSet = (IValueSet)part;
            return true;
        }
        return false;
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        if (xmlTag.getNodeName().equals(ValueSet.XML_TAG)) {
            valueSet = ValueSetType.newValueSet(xmlTag, this, id);
            return valueSet;
        }
        return null;
    }

    @Override
    protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        return null;
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        if (part instanceof IValueSet) {
            valueSet = new UnrestrictedValueSet(this, getNextPartId());
            return true;
        }
        return false;
    }

    @Override
    protected void reinitPartCollectionsThis() {
        // Nothing to do
    }

    @Override
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) {
        return getProductCmptType();
    }

    @Override
    public boolean isPolicyCmptTypeProperty() {
        return false;
    }

    @Override
    public boolean isPropertyFor(IPropertyValue propertyValue) {
        return getProductCmptPropertyType().isMatchingPropertyValue(getPropertyName(), propertyValue);
    }

    @Override
    protected void validateThis(MessageList result, IIpsProject ipsProject) {
        super.validateThis(result, ipsProject);
        validateAllowedValueSetTypes(result);
        validateOverwriteFlag(result, ipsProject);
        validateChangingOverTimeFlag(result);
    }

    private void validateAllowedValueSetTypes(MessageList result) {
        if (!getAllowedValueSetTypes(getIpsProject()).contains(getValueSet().getValueSetType())) {
            result.add(
                    Message.newError(MSGCODE_INVALID_VALUE_SET,
                            MessageFormat.format(Messages.ProductCmptTypeAttribute_msg_invalidValueSet,
                                    getValueSet().getValueSetType().getName(), getPropertyName()),
                            this, PROPERTY_VALUE_SET));
        }
    }

    private void validateOverwriteFlag(MessageList result, IIpsProject ipsProject) {
        if (isOverwrite()) {
            IProductCmptTypeAttribute superAttr = (IProductCmptTypeAttribute)findOverwrittenAttribute(ipsProject);
            if (superAttr != null) {
                if (isMultiValueAttribute() != superAttr.isMultiValueAttribute()) {
                    result.add(new Message(MSGCODE_OVERWRITTEN_ATTRIBUTE_SINGE_MULTI_VALUE_DIFFERES,
                            Messages.ProductCmptTypeAttribute_msgOverwritten_singleValueMultipleValuesDifference,
                            Message.ERROR, this, PROPERTY_MULTI_VALUE_ATTRIBUTE));
                }
                if (isMultilingual() != superAttr.isMultilingual()) {
                    result.add(Message.newError(MSGCODE_OVERWRITTEN_ATTRIBUTE_MULTILINGUAL_DIFFERS,
                            Messages.ProductCmptTypeAttribute_msgOverwritten_multilingual_different, this,
                            PROPERTY_MULTILINGUAL));
                }
            }
        }
    }

    private void validateChangingOverTimeFlag(MessageList result) {
        ChangingOverTimePropertyValidator propertyValidator = new ChangingOverTimePropertyValidator(this);
        propertyValidator.validateTypeDoesNotAcceptChangingOverTime(result);
    }

    @Override
    protected void validateDefaultValue(ValueDatatype valueDatatype, MessageList result, IIpsProject ipsProject) {
        if (isMultiValueAttribute() && getDefaultValue() != null) {
            validateMultiDefaultValues(valueDatatype, result, ipsProject);
        } else {
            super.validateDefaultValue(valueDatatype, result, ipsProject);
        }
    }

    private void validateMultiDefaultValues(ValueDatatype valueDatatype, MessageList result, IIpsProject ipsProject) {
        String[] split = MultiValueHolder.Factory.getSplitMultiValue(getDefaultValue());
        for (String singleValue : split) {
            validateDefaultValue(singleValue, valueDatatype, result, ipsProject);
        }
    }

    @Override
    protected void validateDefaultValue(String defaultValueToValidate,
            ValueDatatype valueDatatype,
            MessageList result,
            IIpsProject ipsProject) {
        super.validateDefaultValue(defaultValueToValidate, valueDatatype, result, ipsProject);
        validateDefaultValue(defaultValueToValidate, result);
    }

    private void validateDefaultValue(String defaultValue, MessageList result) {
        if (!isVisible() && !getValueSet().containsValue(defaultValue, getIpsProject())) {
            result.remove(result.getMessageByCode(MSGCODE_DEFAULT_NOT_IN_VALUESET));
            result.newError(MSGCODE_DEFAULT_NOT_IN_VALUESET_WHILE_HIDDEN,
                    MessageFormat.format(Messages.ProductCmptTypeAttribute_msgDefaultValueNotInValueSetWhileHidden,
                            defaultValue),
                    this, PROPERTY_DEFAULT_VALUE);
        }
    }

    @Override
    public boolean isChangingOverTimeValidationNecessary() {
        return true;
    }

}
