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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.values.Decimal;

/**
 * Format for floating point number input. This formatter is valid for {@link Decimal},
 * {@link Double} and {@link BigDecimal}
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
        if (valueAsObject instanceof Decimal) {
            Decimal decimalValue = (Decimal)valueAsObject;
            if (decimalValue.isNull()) {
                return null;
            } else {
                valueAsObject = decimalValue.bigDecimalValue();
            }
        }
        String stringToBeDisplayed = numberFormat.format(valueAsObject);
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
