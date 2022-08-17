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
 * Tests for conversion of an arbitrary CSV-double representation to IPS Double Datatype and vice
 * versa.
 * 
 * @author Roman Grutza
 */
public class DoubleValueConverterTest extends NumberValueConverterTest {

    @Override
    public ValueDatatype getDatatypeUsedForConversion() {
        return Datatype.DOUBLE;
    }

    @Override
    public String[] getExternalDataToConvert(boolean useCommaAsDecimalSeparator) {
        String[] validExternalDoubles = { String.valueOf(Double.MAX_VALUE).replace(".", ","),
                String.valueOf(Double.MIN_VALUE).replace(".", ","), "42", "42,42", "-42,003E-03" };
        if (useCommaAsDecimalSeparator) {
            for (int i = 0; i < validExternalDoubles.length; i++) {
                validExternalDoubles[i] = validExternalDoubles[i].replace(".", ",");
            }
        }
        return validExternalDoubles;
    }

}
