/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.csv;

import static org.junit.Assert.assertFalse;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.tableconversion.IValueConverter;
import org.faktorips.util.message.MessageList;
import org.junit.Test;

/**
 * Tests for Conversion of an arbitrary CSV-Integer representation to IPS Integer Datatype and vice
 * versa.
 * 
 * @author Roman Grutza
 */
public class IntegerValueConverterTest extends NumberValueConverterTest {

    @Override
    public ValueDatatype getDatatypeUsedForConversion() {
        return Datatype.INTEGER;
    }

    @Override
    public String[] getExternalDataToConvert(boolean useCommaAsDecimalSeparator) {
        String[] validExternalDoubles = { String.valueOf(Integer.MAX_VALUE), String.valueOf(Integer.MIN_VALUE), "0" };
        return validExternalDoubles;
    }

    @Test
    public void testExternalToInternalOverflow() {
        String[] doNotFitInInteger = { String.valueOf(Long.MAX_VALUE), String.valueOf(Double.MIN_VALUE) };

        MessageList ml = new MessageList();
        IValueConverter converter = new IntegerValueConverter();
        for (String element : doNotFitInInteger) {
            String ipsValue = converter.getIpsValue(element, ml);
            assertFalse(ml.isEmpty());
            assertFalse(Datatype.INTEGER.isParsable(ipsValue));
        }
    }

}
