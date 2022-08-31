/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;
import org.faktorips.devtools.core.ui.controller.fields.FormattingTextField;
import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.productcmpt.DelegatingValueHolder;
import org.faktorips.devtools.model.internal.productcmpt.MultiValueHolder;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.ISingleValueHolder;
import org.faktorips.devtools.model.productcmpt.IValueHolder;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.value.ValueType;
import org.faktorips.values.LocalizedString;

/**
 * Can format the value of an {@link IAttributeValue}. Provides a {@link #getFormattedValue()}
 * method. In case of a single-value attribute {@link #getFormattedValue()} returns the attribute's
 * value as a string formatted depending on the datatype and the locale. In case of a multi-value
 * attribute a list of formatted values is be returned. e.g. a list of the ISO dates "2012-04-01"
 * and "2012-04-02" will be displayed as "[01.04.2012 | 02.04.2012]" in the German locale.
 * <p>
 * Warning: this wrapper can only be used to display human readable values <em>not</em> to edit
 * them. If you want to edit a formatted value use {@link FormattingTextField}.
 * 
 * @see IProductCmptTypeAttribute#isMultiValueAttribute()
 * 
 * @author Stefan Widmaier
 */
public class AttributeValueFormatter {

    public static final String PROPERTY_FORMATTED_VALUE = "formattedValue"; //$NON-NLS-1$

    private static final String MULTI_VALUE_SEPARATOR = " " + MultiValueHolder.SEPARATOR + " "; //$NON-NLS-1$ //$NON-NLS-2$

    private final IAttributeValue attrValue;
    private final ValueDatatype datatype;

    public AttributeValueFormatter(IAttributeValue attrValue, ValueDatatype datatype) {
        this.attrValue = attrValue;
        this.datatype = datatype;
    }

    /**
     * Utility method. Shorthand for
     * 
     * <pre>
     * AttributValueFormatter#createFormatterFor(IAttributeValue)#getFormattedValue()
     * </pre>
     * 
     * @param attrValue the attribute value to format
     * @return the formatted value
     */
    public static String format(IAttributeValue attrValue) {
        return createFormatterFor(attrValue).getFormattedValue();
    }

    public static AttributeValueFormatter createFormatterFor(IAttributeValue attrValue) {
        IProductCmptTypeAttribute pctAttribute;
        pctAttribute = attrValue.findAttribute(attrValue.getIpsProject());
        ValueDatatype datatype;
        if (pctAttribute == null) {
            datatype = Datatype.STRING;
        } else {
            datatype = pctAttribute.findDatatype(attrValue.getIpsProject());
        }
        return new AttributeValueFormatter(attrValue, datatype);
    }

    public String getFormattedValue() {
        UIDatatypeFormatter datatypeFormatter = IpsUIPlugin.getDefault().getDatatypeFormatter();
        IValueHolder<?> valueHolder = getActualValueHolder(attrValue.getValueHolder());
        if (valueHolder instanceof MultiValueHolder) {
            MultiValueHolder multiHolder = (MultiValueHolder)valueHolder;
            List<String> stringValues = new ArrayList<>();
            for (ISingleValueHolder holder : multiHolder.getValue()) {
                String formattedValue = getFormattedSingleValue(datatypeFormatter, holder);
                stringValues.add(formattedValue);
            }
            return convertToString(stringValues);
        } else if (valueHolder instanceof ISingleValueHolder) {
            return getFormattedSingleValue(datatypeFormatter, (ISingleValueHolder)valueHolder);
        } else {
            throw new IllegalStateException("Illegal value holder " + valueHolder); //$NON-NLS-1$
        }
    }

    private String getFormattedSingleValue(UIDatatypeFormatter datatypeFormatter, ISingleValueHolder holder) {
        String stringValue;
        if (holder.getValueType() == ValueType.INTERNATIONAL_STRING) {
            LocalizedString locString = ((IInternationalString)holder.getValue().getContent()).get(
                    IIpsModel.get().getMultiLanguageSupport().getLocalizationLocaleOrDefault(holder.getIpsProject()));
            stringValue = locString.getValue();
        } else {
            stringValue = holder.getStringValue();
        }
        return datatypeFormatter.formatValue(datatype, stringValue);
    }

    private IValueHolder<?> getActualValueHolder(IValueHolder<?> v) {
        if (v instanceof DelegatingValueHolder) {
            return getActualValueHolder(((DelegatingValueHolder<?>)v).getDelegate());
        }
        return v;
    }

    protected String convertToString(List<String> stringValues) {
        return StringUtils.join(stringValues, MULTI_VALUE_SEPARATOR);
    }

}
