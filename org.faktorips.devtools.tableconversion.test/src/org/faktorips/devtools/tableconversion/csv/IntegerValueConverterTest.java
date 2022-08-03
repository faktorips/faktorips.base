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

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.tableconversion.IValueConverter;
import org.faktorips.runtime.MessageList;
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
        return new String[] { String.valueOf(Integer.MAX_VALUE), String.valueOf(Integer.MIN_VALUE), "0" };
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
