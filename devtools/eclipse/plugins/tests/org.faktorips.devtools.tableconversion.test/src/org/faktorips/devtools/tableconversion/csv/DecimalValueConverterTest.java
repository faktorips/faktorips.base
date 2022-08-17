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

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;

/**
 * Tests for Conversion of an arbitrary CSV-decimal representation to IPS Decimal Datatype and vice
 * versa.
 * 
 * @author Roman Grutza
 */
public class DecimalValueConverterTest extends NumberValueConverterTest {

    @Override
    public ValueDatatype getDatatypeUsedForConversion() {
        return Datatype.DECIMAL;
    }

    @Override
    public String[] getExternalDataToConvert(boolean useCommaAsDecimalSeparator) {
        String[] validExternalDoubles = { "1234", String.valueOf(Long.MAX_VALUE), String.valueOf(Long.MIN_VALUE),
                "1234567890.0987654321" };
        if (useCommaAsDecimalSeparator) {
            for (int i = 0; i < validExternalDoubles.length; i++) {
                validExternalDoubles[i] = validExternalDoubles[i].replace(".", ",");
            }
        }
        return validExternalDoubles;
    }
}
