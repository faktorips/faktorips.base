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
import java.util.Locale;

import org.faktorips.devtools.model.internal.InternationalString;
import org.faktorips.devtools.model.internal.productcmpt.MultiValueHolder;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.internal.value.InternationalStringValue;
import org.faktorips.devtools.model.internal.value.StringValue;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.productcmpt.DeltaType;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.ISingleValueHolder;
import org.faktorips.devtools.model.productcmpt.IValueHolder;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.value.ValueType;
import org.faktorips.values.LocalizedString;

/**
 * This delta entry describes a mismatch of the value between an attribute and its attribute value.
 * For example the attribute is marked as multilingual attribute but the value holder value is an
 * StringValue.
 * 
 * 
 * @author frank
 * @since 3.9
 */
public class MultilingualMismatchEntry extends AbstractDeltaEntryForProperty {

    private final IProductCmptTypeAttribute attribute;
    private final Locale defaultLanguageLocale;

    public MultilingualMismatchEntry(IAttributeValue attributeValue, IProductCmptTypeAttribute attribute) {
        super(attributeValue);
        this.attribute = attribute;
        defaultLanguageLocale = getDefaultLanguage();
    }

    private Locale getDefaultLanguage() {
        ISupportedLanguage supportedLanguage = getPropertyValue().getIpsProject().getReadOnlyProperties()
                .getDefaultLanguage();
        return supportedLanguage.getLocale();
    }

    @Override
    public IAttributeValue getPropertyValue() {
        return (IAttributeValue)super.getPropertyValue();
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.MULTILINGUAL_MISMATCH;
    }

    @Override
    public String getDescription() {
        String defaultLanguage = defaultLanguageLocale.getDisplayLanguage();
        String msg;
        if (attribute.isMultilingual()) {
            msg = Messages.MultilingualMismatchEntry_convertToInternatlStringValue;
        } else {
            msg = Messages.MultilingualMismatchEntry_convertToStringValue;
        }
        return MessageFormat.format(msg, getPropertyName(), defaultLanguage);
    }

    @Override
    public void fix() {
        IValueHolder<?> value = getPropertyValue().getValueHolder();
        if (value instanceof MultiValueHolder) {
            MultiValueHolder multiValue = (MultiValueHolder)value;
            for (ISingleValueHolder valueHolder : multiValue.getValue()) {
                setNewValueInSingleValueHolder(valueHolder);
            }
        } else if (value instanceof SingleValueHolder) {
            SingleValueHolder valueHolder = (SingleValueHolder)value;
            setNewValueInSingleValueHolder(valueHolder);
        }
    }

    private void setNewValueInSingleValueHolder(ISingleValueHolder valueHolder) {
        ValueType valueType = valueHolder.getValueType();
        if (ValueType.STRING.equals(valueType)) {
            setNewInternationalStringValue(valueHolder);
        } else if (ValueType.INTERNATIONAL_STRING.equals(valueType)) {
            setNewStringValue(valueHolder);
        }
    }

    private void setNewInternationalStringValue(ISingleValueHolder valueHolder) {
        InternationalStringValue newInternationalStringValue = new InternationalStringValue();
        if (valueHolder.getValue() != null) {
            String oldStringValue = (String)valueHolder.getValue().getContent();
            if (oldStringValue != null) {
                newInternationalStringValue.getContent()
                        .add(new LocalizedString(defaultLanguageLocale, oldStringValue));
            }
        }
        valueHolder.setValue(newInternationalStringValue);
    }

    private void setNewStringValue(ISingleValueHolder valueHolder) {
        String newValue = null;
        if (valueHolder.getValue() != null) {
            InternationalString oldValue = (InternationalString)valueHolder.getValue().getContent();
            if (oldValue != null) {
                LocalizedString iLocalizedString = oldValue.get(defaultLanguageLocale);
                newValue = iLocalizedString.getValue();
            }
        }
        valueHolder.setValue(new StringValue(newValue));
    }
}
