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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.model.IInternationalString;
import org.faktorips.devtools.core.model.ILocalizedString;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.value.ValueType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;
import org.faktorips.devtools.core.ui.controller.fields.FormattingTextField;

/**
 * A Wrapper for an {@link IAttributeValue}. Provides a {@link #getFormattedValue()} method. In case
 * of a single-value attribute {@link #getFormattedValue()} returns the attribute's value as a
 * string formatted depending on the datatype and the locale. In case of a multi-value attribute a
 * list of formatted values is be returned. e.g. a list of the ISO dates "2012-04-01" and
 * "2012-04-02" will be displayed as "[01.04.2012 | 02.04.2012]" in the German locale.
 * <p/>
 * Warning: this wrapper can only be used to display human readable values <em>not</em> to edit
 * them. If you want to edit a formatted value use {@link FormattingTextField}.
 * 
 * @see IProductCmptTypeAttribute#isMultiValueAttribute()
 * 
 * @author Stefan Widmaier
 */
public class ValueHolderToFormattedStringWrapper {

    private static final String MULTI_VALUE_SEPARATOR = " | "; //$NON-NLS-1$
    public static final String PROPERTY_FORMATTED_VALUE = "formattedValue"; //$NON-NLS-1$
    private final IAttributeValue attrValue;
    private final ValueDatatype datatype;

    public ValueHolderToFormattedStringWrapper(IAttributeValue attrValue, ValueDatatype datatype) {
        this.attrValue = attrValue;
        this.datatype = datatype;
    }

    public static ValueHolderToFormattedStringWrapper createWrapperFor(IAttributeValue attrValue) {
        IProductCmptTypeAttribute pctAttribute;
        try {
            pctAttribute = attrValue.findAttribute(attrValue.getIpsProject());
            ValueDatatype datatype;
            if (pctAttribute == null) {
                datatype = Datatype.STRING;
            } else {
                datatype = pctAttribute.findDatatype(attrValue.getIpsProject());
            }
            return new ValueHolderToFormattedStringWrapper(attrValue, datatype);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public String getFormattedValue() {
        UIDatatypeFormatter datatypeFormatter = IpsUIPlugin.getDefault().getDatatypeFormatter();
        IValueHolder<?> valueHolder = attrValue.getValueHolder();
        if (valueHolder instanceof MultiValueHolder) {
            MultiValueHolder multiHolder = (MultiValueHolder)valueHolder;
            List<String> stringValues = new ArrayList<String>();
            for (SingleValueHolder holder : multiHolder.getValue()) {
                String stringValue;
                if (holder.getValueType() == ValueType.INTERNATIONAL_STRING) {
                    ILocalizedString locString = ((IInternationalString)holder.getValue().getContent()).get(IpsPlugin
                            .getMultiLanguageSupport().getLocalizationLocaleOrDefault(holder.getIpsProject()));
                    stringValue = locString == null ? null : locString.getValue();
                } else {
                    stringValue = holder.getStringValue();
                }
                String formattedValue = datatypeFormatter.formatValue(datatype, stringValue);
                stringValues.add(formattedValue);
            }
            return convertToString(stringValues);
        } else {
            return datatypeFormatter.formatValue(datatype, ((SingleValueHolder)valueHolder).getStringValue());
        }
    }

    protected String convertToString(List<String> stringValues) {
        StringBuffer sb = new StringBuffer();
        sb.append("["); //$NON-NLS-1$
        for (String string : stringValues) {
            sb.append(string);
            sb.append(MULTI_VALUE_SEPARATOR);
        }
        if (!stringValues.isEmpty()) {
            // delete redundant MULTI_VALUE_SEPARATOR
            sb.delete(sb.length() - MULTI_VALUE_SEPARATOR.length(), sb.length());
        }
        sb.append("]"); //$NON-NLS-1$
        return sb.toString();
    }

}
