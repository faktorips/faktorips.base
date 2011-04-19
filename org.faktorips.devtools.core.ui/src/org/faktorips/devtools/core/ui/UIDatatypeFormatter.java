/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.BigDecimalDatatype;
import org.faktorips.datatype.classtypes.DateDatatype;
import org.faktorips.datatype.classtypes.DecimalDatatype;
import org.faktorips.datatype.classtypes.DoubleDatatype;
import org.faktorips.datatype.classtypes.GregorianCalendarDatatype;
import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.datatype.classtypes.LongDatatype;
import org.faktorips.datatype.classtypes.MoneyDatatype;
import org.faktorips.devtools.core.DatatypeFormatter;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.internal.model.valueset.Messages;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IUnrestrictedValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.controller.fields.DecimalNumberFormat;
import org.faktorips.devtools.core.ui.controller.fields.DateISOStringFormat;
import org.faktorips.devtools.core.ui.controller.fields.IntegerNumberFormat;
import org.faktorips.devtools.core.ui.controller.fields.MoneyFormat;

public class UIDatatypeFormatter {

    /**
     * Formats the given value according to the user preferences.
     * <p>
     * Note that this method delegates most calls to {@link DatatypeFormatter} in the core plugin.
     * Eventually the {@link DatatypeFormatter}-code should be moved to this class. See MTS#530
     * 
     * @param datatype The data type the value is a value of.
     * @param value The value as string
     */
    public String formatValue(ValueDatatype datatype, String value) {
        if (value == null) {
            return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        }
        if (datatype == null) {
            return value;
        }
        if (datatype instanceof DoubleDatatype || datatype instanceof DecimalDatatype
                || datatype instanceof BigDecimalDatatype) {
            return DecimalNumberFormat.newInstance(datatype).format(value);
        }
        if (datatype instanceof GregorianCalendarDatatype || datatype instanceof DateDatatype) {
            return new DateISOStringFormat().format(value);
        }
        if (datatype instanceof IntegerDatatype || datatype instanceof LongDatatype
                || datatype == ValueDatatype.PRIMITIVE_INT || datatype == ValueDatatype.PRIMITIVE_LONG) {
            return IntegerNumberFormat.newInstance(datatype).format(value);
        }
        if (datatype instanceof MoneyDatatype) {
            MoneyFormat moneyFormat = MoneyFormat.newInstance(null);
            moneyFormat.setAddCurrencySymbol(true);
            String result = moneyFormat.format(value);
            return result;
        }
        return IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter().formatValue(datatype, value);
    }

    public String formatValueSet(IValueSet valueSet) {
        if (valueSet instanceof EnumValueSet) {
            EnumValueSet enumValueSet = (EnumValueSet)valueSet;
            ValueDatatype type = enumValueSet.getValueDatatype();
            StringBuffer buffer = new StringBuffer();
            buffer.append("["); //$NON-NLS-1$
            for (String id : enumValueSet.getValues()) {
                String formatedEnumText = formatValue(type, id);
                buffer.append(formatedEnumText);
                buffer.append(" | "); //$NON-NLS-1$
            }
            if (buffer.length() > 3) {
                /*
                 * Remove the separator after the last value (" | ")
                 */
                buffer.delete(buffer.length() - 3, buffer.length());
            }
            buffer.append("]"); //$NON-NLS-1$
            return buffer.toString();
        } else if (valueSet instanceof IRangeValueSet) {
            RangeValueSet rangeValueSet = (RangeValueSet)valueSet;
            StringBuffer sb = new StringBuffer();
            sb.append('[');
            sb.append((rangeValueSet.getLowerBound() == null ? "unlimited" : rangeValueSet.getLowerBound())); //$NON-NLS-1$
            sb.append('-');
            sb.append((rangeValueSet.getUpperBound() == null ? "unlimited" : rangeValueSet.getUpperBound())); //$NON-NLS-1$
            sb.append(']');
            if (rangeValueSet.getStep() != null) {
                sb.append(Messages.RangeValueSet_0);
                sb.append(rangeValueSet.getStep());
            }
            return sb.toString();
        } else if (valueSet instanceof IUnrestrictedValueSet) {
            return "[" + org.faktorips.devtools.core.model.valueset.Messages.ValueSetType__allValues + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return ""; //$NON-NLS-1$
    }
}
