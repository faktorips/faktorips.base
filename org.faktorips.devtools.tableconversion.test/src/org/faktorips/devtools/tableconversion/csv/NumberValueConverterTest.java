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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.tableconversion.ITableFormat;
import org.faktorips.devtools.core.tableconversion.IValueConverter;
import org.faktorips.runtime.MessageList;
import org.junit.Test;

public abstract class NumberValueConverterTest {

    /**
     * @return The datatype to pass into <code>ITableFormat.getIpsValue()</code> and
     *             <code>ITableFormat.getExternalValue()</code> when doing conversions.
     */
    public abstract ValueDatatype getDatatypeUsedForConversion();

    /**
     * @param useCommaAsDecimalSeparator If <code>true</code> the returned numbers use a comma as
     *            decimal separator character (e.g. 3,456), otherwise a dot is used (3.456).
     * 
     * @return Test data in an external representation to be converted into the internal IPS
     *             representation using <code>IValueConverter.getIpsValue()</code>.
     */
    public abstract String[] getExternalDataToConvert(boolean useCommaAsDecimalSeparator);

    @Test
    public void testExternalToInternal() {
        String[] validExternalDoubles = getExternalDataToConvert(false);

        ITableFormat tableFormat = getCSVTableFormat();

        MessageList ml = new MessageList();
        for (String validExternalDouble : validExternalDoubles) {
            String ipsValue = tableFormat.getIpsValue(validExternalDouble, getDatatypeUsedForConversion(), ml);
            assertTrue(ml.isEmpty());
            assertTrue(getDatatypeUsedForConversion().isParsable(ipsValue));
        }
    }

    @Test
    public void testExternalToInternalCustomDecimalFormat() {
        String[] validExternalDoubles = getExternalDataToConvert(true);

        ITableFormat tableFormat = getCSVTableFormat();
        tableFormat.setProperty(CSVTableFormat.PROPERTY_DECIMAL_SEPARATOR_CHAR, ",");

        MessageList ml = new MessageList();
        for (String validExternalDouble : validExternalDoubles) {
            String ipsValue = tableFormat.getIpsValue(validExternalDouble, getDatatypeUsedForConversion(), ml);
            assertTrue(ml.isEmpty());
            assertTrue(getDatatypeUsedForConversion().isParsable(ipsValue));
        }
    }

    private ITableFormat getCSVTableFormat() {
        ITableFormat tableFormat = null;
        for (ITableFormat tf : IpsPlugin.getDefault().getExternalTableFormats()) {
            if (tf instanceof CSVTableFormat) {
                tableFormat = tf;
            }
        }
        return tableFormat;
    }

    @Test
    public void testExternalToInternalInvalid() {
        String[] validExternalDecimals = { "Egon" };

        MessageList ml = new MessageList();
        IValueConverter converter = new DecimalValueConverter();
        for (String validExternalDecimal : validExternalDecimals) {
            String ipsValue = converter.getIpsValue(validExternalDecimal, ml);
            assertFalse(ml.isEmpty());
            assertFalse(Datatype.DECIMAL.isParsable(ipsValue));
        }
    }

}
