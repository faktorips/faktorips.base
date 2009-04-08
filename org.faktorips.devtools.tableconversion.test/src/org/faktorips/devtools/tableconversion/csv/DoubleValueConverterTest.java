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
import org.faktorips.devtools.tableconversion.IValueConverter;
import org.faktorips.util.message.MessageList;

/**
 * Tests for Conversion of an arbitrary CSV-double representation to IPS Double Datatype 
 * 
 * @author Roman Grutza
 */
public class DoubleValueConverterTest extends TestCase {

    public void testExternalToInternal() {
        String[] validExternalDoubles = {
                String.valueOf(Double.MAX_VALUE),
                String.valueOf(Double.MIN_VALUE),
                "42", "42.42", "-42.003E-03"
        };
        
        MessageList ml = new MessageList();
        IValueConverter converter = new DoubleValueConverter();
        for (int i = 0; i < validExternalDoubles.length; i++) {
            String ipsValue = converter.getIpsValue(validExternalDoubles[i], ml);
            assertTrue(ml.isEmpty());
            assertTrue(Datatype.DOUBLE.isParsable(ipsValue));
        }
    }
    
    public void testLocalizedExternalToInternal() {
        String[] localizedExternalDoubles = {
                "1,233"
        };
        
        IValueConverter converter = new DoubleValueConverter( /* Locale.FR */ );
        // ...
    }
    
}
