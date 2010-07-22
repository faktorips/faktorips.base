/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
