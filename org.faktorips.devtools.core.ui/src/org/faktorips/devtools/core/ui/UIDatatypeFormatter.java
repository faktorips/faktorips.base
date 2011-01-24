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
import org.faktorips.devtools.core.DatatypeFormatter;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.controller.fields.DateISOStringFormat;
import org.faktorips.devtools.core.ui.controller.fields.DoubleFormat;
import org.faktorips.devtools.core.ui.controller.fields.IntegerFormat;

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
            return new DoubleFormat().format(value);
        }
        if (datatype instanceof GregorianCalendarDatatype || datatype instanceof DateDatatype) {
            return new DateISOStringFormat().format(value);
        }
        if (datatype instanceof IntegerDatatype || datatype instanceof LongDatatype
                || datatype == ValueDatatype.PRIMITIVE_INT || datatype == ValueDatatype.PRIMITIVE_LONG) {
            return new IntegerFormat().format(value);
        }
        return IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter().formatValue(datatype, value);
    }
}
