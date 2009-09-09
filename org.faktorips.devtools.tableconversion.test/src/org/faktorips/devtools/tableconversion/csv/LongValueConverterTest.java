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

import java.math.BigDecimal;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.tableconversion.IValueConverter;
import org.faktorips.util.message.MessageList;

/**
 * Tests for Conversion of an arbitrary CSV-Long representation to IPS Long Datatype and vice versa.
 * 
 * @author Roman Grutza
 */
public class LongValueConverterTest extends NumberValueConverterTest {

    @Override
    public ValueDatatype getDatatypeUsedForConversion() {
        return Datatype.LONG;
    }

    @Override
    public String[] getExternalDataToConvert(boolean useCommaAsDecimalSeparator) {
        String[] validExternalDoubles = {
                String.valueOf(Long.MAX_VALUE),
                String.valueOf(Long.MIN_VALUE),
                "0"
        };
        return validExternalDoubles;
    }

    public void testExternalToInternalOverflow() {
        String tooBig = new BigDecimal(Long.MAX_VALUE).multiply(new BigDecimal(2)).toString();

        MessageList ml = new MessageList();
        IValueConverter converter = new IntegerValueConverter();
        String ipsValue = converter.getIpsValue(tooBig, ml);
        assertFalse(ml.toString(), ml.isEmpty());
        assertFalse(Datatype.INTEGER.isParsable(ipsValue));
    }

}
