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

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.faktorips.datatype.ValueDatatype;

/**
 * Format for floating point number input.
 * 
 * @author Stefan Widmaier
 */
public class DecimalNumberFormat extends AbstractNumberFormat {

    private DecimalFormat numberFormat;
    private final ValueDatatype datatype;

    public static DecimalNumberFormat newInstance(ValueDatatype datatype) {
        DecimalNumberFormat bigDecimalFormat = new DecimalNumberFormat(datatype);
        bigDecimalFormat.initFormat();
        return bigDecimalFormat;
    }

    protected DecimalNumberFormat(ValueDatatype datatype) {
        this.datatype = datatype;
    }

    /**
     * String that is an example of a valid input string.
     */
    private String exampleString;

    @Override
    protected void initFormat(Locale locale) {
        numberFormat = (DecimalFormat)NumberFormat.getNumberInstance(locale);
        numberFormat.setGroupingUsed(true);
        numberFormat.setGroupingSize(3);
        numberFormat.setParseIntegerOnly(false);
        numberFormat.setMaximumFractionDigits(Integer.MAX_VALUE);
        numberFormat.setRoundingMode(RoundingMode.HALF_UP);
        numberFormat.setParseBigDecimal(true);
        exampleString = numberFormat.format(-1000.2);
    }

    @Override
    protected String formatInternal(String value) {
        Object valueAsObject = datatype.getValue(value);
        // BigDecimal valueAsObject = new BigDecimal(value);
        // Double valueAsObject = Double.parseDouble(value);
        // System.out.print("Value \"" + d + "\" is being parsed...");
        String stringToBeDisplayed = numberFormat.format(valueAsObject);
        // System.out.println("Value \"" + stringToBeDisplayed + "\" will be displayed");
        return stringToBeDisplayed;
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
