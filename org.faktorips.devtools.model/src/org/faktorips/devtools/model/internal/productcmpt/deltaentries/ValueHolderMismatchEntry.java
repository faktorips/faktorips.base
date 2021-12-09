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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.model.internal.productcmpt.MultiValueHolder;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.productcmpt.DeltaType;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IMultiValueHolder;
import org.faktorips.devtools.model.productcmpt.ISingleValueHolder;
import org.faktorips.devtools.model.productcmpt.IValueHolder;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.value.IValue;

/**
 * This delta entry describes a mismatch of the value between an attribute and its attribute value.
 * For example the attribute is marked as multi value attribute but the attribute value has a single
 * value.
 * 
 * @author dirmeier
 */
public class ValueHolderMismatchEntry extends AbstractDeltaEntryForProperty {

    private final IProductCmptTypeAttribute attribute;

    public ValueHolderMismatchEntry(IAttributeValue attributeValue, IProductCmptTypeAttribute attribute) {
        super(attributeValue);
        this.attribute = attribute;
    }

    @Override
    public IAttributeValue getPropertyValue() {
        return (IAttributeValue)super.getPropertyValue();
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.VALUE_HOLDER_MISMATCH;
    }

    @Override
    public String getDescription() {
        if (attribute.isMultiValueAttribute()) {
            return MessageFormat.format(Messages.ValueMismatchEntry_convertSingleToMultiValue, getPropertyName());
        } else {
            return MessageFormat.format(Messages.ValueMismatchEntry_convertMultiToSingleValue, getPropertyName());
        }
    }

    @Override
    public void fix() {
        IValueHolder<?> value = getPropertyValue().getValueHolder();
        if (!attribute.isMultiValueAttribute() && value instanceof MultiValueHolder) {
            MultiValueHolder multiValue = (MultiValueHolder)value;
            IValue<?> oldValue = null;
            if (!multiValue.getValue().isEmpty()) {
                oldValue = multiValue.getValue().get(0).getValue();
            }
            SingleValueHolder newValue = new SingleValueHolder(getPropertyValue(), oldValue);
            getPropertyValue().setValueHolder(newValue);
        } else if (attribute.isMultiValueAttribute() && value instanceof SingleValueHolder) {
            List<ISingleValueHolder> oldValueList = new ArrayList<>();
            SingleValueHolder singleValue = (SingleValueHolder)value;
            if (singleValue.getValue() != null) {
                oldValueList.add(singleValue);
            }
            IMultiValueHolder newValue = new MultiValueHolder(getPropertyValue(), oldValueList);
            getPropertyValue().setValueHolder(newValue);
        }
    }

}
