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

import junit.framework.TestCase;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.devtools.tableconversion.IValueConverter;
import org.faktorips.util.message.MessageList;

public abstract class NumberValueConverterTest extends TestCase {

    /**
     * @return The datatype to pass into <code>ITableFormat.getIpsValue()</code> and
     * <code>ITableFormat.getExternalValue()</code> when doing conversions.
     */
    public abstract ValueDatatype getDatatypeUsedForConversion();

    /**
     * @param useCommaAsDecimalSeparator If <code>true</code> the returned numbers use a comma
     * as decimal separator character (e.g. 3,456), otherwise a dot is used (3.456).
     * 
     * @return Test data in an external representation to be converted into the internal IPS representation using
     * <code>IValueConverter.getIpsValue()</code>.
     */
    public abstract String[] getExternalDataToConvert(boolean useCommaAsDecimalSeparator);


    public void testExternalToInternal() {
        String[] validExternalDoubles = getExternalDataToConvert(false);

        ITableFormat tableFormat = getCSVTableFormat();

        MessageList ml = new MessageList();
        for (int i = 0; i < validExternalDoubles.length; i++) {
            String ipsValue = tableFormat.getIpsValue(
                    validExternalDoubles[i], getDatatypeUsedForConversion(), ml);
            assertTrue(ml.isEmpty());
            assertTrue(getDatatypeUsedForConversion().isParsable(ipsValue));
        }
    }

    public void testExternalToInternalCustomDecimalFormat() {
        String[] validExternalDoubles = getExternalDataToConvert(true);

        ITableFormat tableFormat = getCSVTableFormat();
        tableFormat.setProperty(CSVTableFormat.PROPERTY_DECIMAL_SEPARATOR_CHAR, ",");

        MessageList ml = new MessageList();
        for (int i = 0; i < validExternalDoubles.length; i++) {
            String ipsValue = tableFormat.getIpsValue(
                    validExternalDoubles[i], getDatatypeUsedForConversion(), ml);
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

    public void testExternalToInternalInvalid() {
        String[] validExternalDecimals = { "Egon" };

        MessageList ml = new MessageList();
        IValueConverter converter = new DecimalValueConverter();
        for (int i = 0; i < validExternalDecimals.length; i++) {
            String ipsValue = converter.getIpsValue(validExternalDecimals[i], ml);
            assertFalse(ml.isEmpty());
            assertFalse(Datatype.DECIMAL.isParsable(ipsValue));
        }
    }

}