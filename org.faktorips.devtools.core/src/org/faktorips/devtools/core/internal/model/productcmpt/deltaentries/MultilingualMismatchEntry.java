/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import java.util.Locale;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.InternationalString;
import org.faktorips.devtools.core.internal.model.LocalizedString;
import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.internal.model.value.InternationalStringValue;
import org.faktorips.devtools.core.internal.model.value.StringValue;
import org.faktorips.devtools.core.model.ILocalizedString;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.value.ValueType;

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
        this.defaultLanguageLocale = getDefaultLanguage();
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
        return NLS.bind(msg, new String[] { getPropertyName(), defaultLanguage });
    }

    @Override
    public void fix() {
        IValueHolder<?> value = getPropertyValue().getValueHolder();
        if (value instanceof MultiValueHolder) {
            MultiValueHolder multiValue = (MultiValueHolder)value;
            for (SingleValueHolder valueHolder : multiValue.getValue()) {
                setNewValueInSingleValueHolder(valueHolder);
            }
        } else if (value instanceof SingleValueHolder) {
            SingleValueHolder valueHolder = (SingleValueHolder)value;
            setNewValueInSingleValueHolder(valueHolder);
        }
    }

    private void setNewValueInSingleValueHolder(SingleValueHolder valueHolder) {
        ValueType valueType = valueHolder.getValueType();
        if (ValueType.STRING.equals(valueType)) {
            setNewInternationalStringValue(valueHolder);
        } else if (ValueType.INTERNATIONAL_STRING.equals(valueType)) {
            setNewStringValue(valueHolder);
        }
    }

    private void setNewInternationalStringValue(SingleValueHolder valueHolder) {
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

    private void setNewStringValue(SingleValueHolder valueHolder) {
        String newValue = null;
        if (valueHolder.getValue() != null) {
            InternationalString oldValue = (InternationalString)valueHolder.getValue().getContent();
            if (oldValue != null) {
                ILocalizedString iLocalizedString = oldValue.get(defaultLanguageLocale);
                if (iLocalizedString != null) {
                    newValue = iLocalizedString.getValue();
                }
            }
        }
        valueHolder.setValue(new StringValue(newValue));
    }
}
