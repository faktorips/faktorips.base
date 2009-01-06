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

package org.faktorips.devtools.core.util;

import junit.framework.TestCase;

public class StringUtilsTest extends TestCase {

    public final void testWrapText() {
        
        String text = "Diese Klasse enthält die Kontostände, die für die Bestimmung der abzuführenden \n"
            + "Kapitalertragssteuer gegebenenfalls benötigt werden. Sie wird auf Deckungsebene \n"
            + "geführt. Dabei unterscheidet man zwischen den Komponenten Stammdeckung, \n"
            + "Bonus, Ansammlungsguthaben und Schlussüberschuss.";
        
        String expectedText = "Diese Klasse enthält die Kontostände, die für die Bestimmung der abzuführenden\n"
            + "Kapitalertragssteuer gegebenenfalls benötigt werden. Sie wird auf Deckungsebene\n"
            + "geführt. Dabei unterscheidet man zwischen den Komponenten Stammdeckung, \n"
            + "Bonus, Ansammlungsguthaben und Schlussüberschuss.";

        String wrappedText = StringUtils.wrapText(text, 73, "\n");
        assertEquals(expectedText, wrappedText);
        
        text = "Diese Klasse enthält die Kontostände, die für die Bestimmung der abzuführenden "
            + "Kapitalertragssteuer gegebenenfalls benötigt werden. Sie wird auf Deckungsebene "
            + "geführt. Dabei unterscheidet man zwischen den Komponenten Stammdeckung, "
            + "Bonus, Ansammlungsguthaben und Schlussüberschuss.";

        expectedText = "Diese Klasse enthält die Kontostände, die für die Bestimmung der abzuführenden\n" +
                "Kapitalertragssteuer gegebenenfalls benötigt werden. Sie wird auf Deckungsebene\n" +
                "geführt. Dabei unterscheidet man zwischen den Komponenten Stammdeckung, Bonus,\n" +
                "Ansammlungsguthaben und Schlussüberschuss.";
        
        wrappedText = StringUtils.wrapText(text, 73, "\n");
        assertEquals(expectedText, wrappedText);
    }

}
