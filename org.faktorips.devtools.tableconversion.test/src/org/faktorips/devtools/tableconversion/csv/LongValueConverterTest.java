/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.csv;

import static org.junit.Assert.assertFalse;

import java.math.BigDecimal;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.tableconversion.IValueConverter;
import org.faktorips.util.message.MessageList;
import org.junit.Test;

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
        String[] validExternalDoubles = { String.valueOf(Long.MAX_VALUE), String.valueOf(Long.MIN_VALUE), "0" };
        return validExternalDoubles;
    }

    @Test
    public void testExternalToInternalOverflow() {
        String tooBig = new BigDecimal(Long.MAX_VALUE).multiply(new BigDecimal(2)).toString();

        MessageList ml = new MessageList();
        IValueConverter converter = new IntegerValueConverter();
        String ipsValue = converter.getIpsValue(tooBig, ml);
        assertFalse(ml.toString(), ml.isEmpty());
        assertFalse(Datatype.INTEGER.isParsable(ipsValue));
    }

}
