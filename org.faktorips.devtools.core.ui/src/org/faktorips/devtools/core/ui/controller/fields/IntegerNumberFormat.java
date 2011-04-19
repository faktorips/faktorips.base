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

package org.faktorips.devtools.core.ui.controller.fields;

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

    private DecimalFormat numberFormat;
    private final ValueDatatype datatype;

    public static IntegerNumberFormat newInstance(ValueDatatype datatype) {
        IntegerNumberFormat longFormat = new IntegerNumberFormat(datatype);
        longFormat.initFormat();
        return longFormat;
    }

    protected IntegerNumberFormat(ValueDatatype datatype) {
        this.datatype = datatype;
    }

    /**
     * String that is an example of a valid input string.
     */
    private String exampleString;

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
