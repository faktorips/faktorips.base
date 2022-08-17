/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.inputformat;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.faktorips.datatype.ValueDatatype;

/**
 * Format for integer number input.
 * 
 * @author Stefan Widmaier
 */
public class IntegerNumberFormat extends AbstractNumberFormat {

    /**
     * String that is an example of a valid input string.
     */
    private String exampleString;
    private DecimalFormat numberFormat;
    private final ValueDatatype datatype;

    protected IntegerNumberFormat(ValueDatatype datatype) {
        this.datatype = datatype;
    }

    public static IntegerNumberFormat newInstance(ValueDatatype datatype) {
        IntegerNumberFormat longFormat = new IntegerNumberFormat(datatype);
        longFormat.initFormat();
        return longFormat;
    }

    @Override
    protected void initFormat(Locale locale) {
        numberFormat = (DecimalFormat)NumberFormat.getIntegerInstance(locale);
        // setting grouping size to maximum value to avoid formatting group separators but allow
        // input separators. A single group separator is forbidden (see verifyInternal) but
        // separators with copy&paste should be allowed
        numberFormat.setGroupingSize(Byte.MAX_VALUE);
        exampleString = numberFormat.format(-100000000);
    }

    @Override
    protected String formatInternal(String value) {
        Object valueAsObject = datatype.getValue(value);
        return numberFormat.format(valueAsObject);
    }

    @Override
    protected String getExampleString() {
        return exampleString;
    }

    @Override
    public DecimalFormat getNumberFormat() {
        return numberFormat;
    }

}
