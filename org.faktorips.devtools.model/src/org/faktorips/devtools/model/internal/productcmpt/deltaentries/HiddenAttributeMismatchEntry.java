/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.deltaentries;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.model.productcmpt.AttributeValueType;
import org.faktorips.devtools.model.productcmpt.DeltaType;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IValueHolder;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.util.ArgumentCheck;

/**
 * Class realizing a delta entry concerning hidden Attributes {@link IProductCmptType}. The hidden
 * attribute value should always have the same content as the default value of the corresponding
 * attribute.
 */
public class HiddenAttributeMismatchEntry extends AbstractDeltaEntryForProperty {

    private IProductCmptTypeAttribute attribute;

    public HiddenAttributeMismatchEntry(IAttributeValue attributeValue, IProductCmptTypeAttribute attribute) {
        super(attributeValue);
        this.attribute = attribute;
        ArgumentCheck.notNull(attributeValue);
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.HIDDEN_ATTRIBUTE_MISMATCH;
    }

    @Override
    public String getDescription() {
        return NLS.bind(Messages.HiddenAttributeMismatchEntry_desc,
                new String[] { getPropertyName(), getCurrentAttributeValue(), attribute.getDefaultValue() });
    }

    public String getCurrentAttributeValue() {
        return getAttributeValue().getValueHolder().getStringValue();
    }

    private IAttributeValue getAttributeValue() {
        return (IAttributeValue)getPropertyValue();
    }

    @Override
    public void fix() {
        setAttributeValueToDefault();
    }

    private void setAttributeValueToDefault() {
        IAttributeValue attributeValue = getAttributeValue();
        attributeValue.setValueHolder(createDefaultValueHolder());
    }

    private IValueHolder<?> createDefaultValueHolder() {
        IValue<?> defaultValue = ValueFactory.createValue(attribute.isMultilingual(), attribute.getDefaultValue());
        return createValueHolderFor(defaultValue);
    }

    private IValueHolder<?> createValueHolderFor(IValue<?> defaultValue) {
        AttributeValueType attributeValueType = AttributeValueType.getTypeFor(attribute);
        IValueHolder<?> valueHolder = attributeValueType.newHolderInstance(getAttributeValue(), defaultValue);
        return valueHolder;
    }

    public boolean isMismatch() {
        return isAttributeHidden() && valuesDiffer();
    }

    private boolean valuesDiffer() {
        IAttributeValue attributeValue = getAttributeValue();
        IValueHolder<?> valueHolder = createDefaultValueHolder();
        return attributeValue.getValueHolder() == null
                || !attributeValue.getValueHolder().equalsValueHolder(valueHolder);
    }

    private boolean isAttributeHidden() {
        return !attribute.isVisible();
    }

}