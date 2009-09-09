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

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;

/**
 * Tests for Conversion of an arbitrary CSV-decimal representation to IPS Decimal Datatype and vice versa.
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
        String[] validExternalDoubles = {
                "1234",
                String.valueOf(Long.MAX_VALUE),
                String.valueOf(Long.MIN_VALUE),
                "1234567890.0987654321"
        };
        if (useCommaAsDecimalSeparator) {
            for (int i = 0; i < validExternalDoubles.length; i++) {
                validExternalDoubles[i] = validExternalDoubles[i].replace(".", ",");
            }
        }
        return validExternalDoubles;
    }
}
