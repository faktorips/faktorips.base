/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.csv;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.faktorips.devtools.core.tableconversion.AbstractValueConverter;
import org.faktorips.devtools.core.tableconversion.ITableFormat;

public abstract class NumberValueConverter extends AbstractValueConverter {

    /**
     * Uses the properties of the given table format to retrieve a custom DecimalNumberFormat. One
     * can configure the table format by setting the properties
     * <code>CSVTableFormat.PROPERTY_DECIMAL_SEPARATOR_CHAR</code> and
     * <code>CSVTableFormat.PROPERTY_DECIMAL_GROUPING_CHAR</code> on the given ITableFormat
     * instance.
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

            DecimalFormatSymbols decimalFormatSymbols = decimalFormat.getDecimalFormatSymbols();
            if (decimalSeparator != null && decimalSeparator.length() == 1
                    && !decimalSeparator.equals(decimalGrouping)) {
                decimalFormatSymbols.setDecimalSeparator(decimalSeparator.charAt(0));
            }
            if (decimalGrouping != null && !decimalGrouping.equals(decimalSeparator)) {
                decimalFormatSymbols.setGroupingSeparator(decimalGrouping.charAt(0));
            }
            decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);
        }
        return decimalFormat;
    }
}
