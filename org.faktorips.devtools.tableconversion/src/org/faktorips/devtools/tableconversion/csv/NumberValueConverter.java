/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.tableconversion.csv;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.faktorips.devtools.tableconversion.AbstractValueConverter;
import org.faktorips.devtools.tableconversion.ITableFormat;

public abstract class NumberValueConverter extends AbstractValueConverter {

    /**
     * Uses the properties of the given table format to retrieve a custom DecimalFormat. One can
     * configure the table format by setting the properties
     * <code>CSVTableFormat.PROPERTY_DECIMAL_SEPARATOR_CHAR</code> and
     * <code>CSVTableFormat.PROPERTY_DECIMAL_GROUPING_CHAR</code>.
     * 
     * @param tableFormat
     * 
     * @return A decimal format with custom decimal separator and grouping character symbols,
     *         according to the properties set on the table format. A default decimal format is
     *         returned if the properties are not set.
     */
    protected DecimalFormat getDecimalFormat(ITableFormat tableFormat) {
        DecimalFormat decimalFormat = new DecimalFormat();

        if (tableFormat != null && tableFormat instanceof CSVTableFormat) {
            String decimalSeparator = tableFormat.getProperty(CSVTableFormat.PROPERTY_DECIMAL_SEPARATOR_CHAR);
            String decimalGrouping = tableFormat.getProperty(CSVTableFormat.PROPERTY_DECIMAL_GROUPING_CHAR);
            if (decimalSeparator.length() == 1 && decimalGrouping.length() == 1
                    && !decimalSeparator.equals(decimalGrouping)) {
                DecimalFormatSymbols decimalFormatSymbols = decimalFormat.getDecimalFormatSymbols();
                decimalFormatSymbols.setDecimalSeparator(decimalSeparator.charAt(0));
                decimalFormatSymbols.setGroupingSeparator(decimalGrouping.charAt(0));
            }
        }
        return decimalFormat;
    }
}
