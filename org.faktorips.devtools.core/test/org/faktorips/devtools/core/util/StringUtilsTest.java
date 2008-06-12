/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
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

        String wrappedText = StringUtils.wrapText(text, 80, 4, "\n");
        assertEquals(expectedText, wrappedText);
        
        text = "Diese Klasse enthält die Kontostände, die für die Bestimmung der abzuführenden "
            + "Kapitalertragssteuer gegebenenfalls benötigt werden. Sie wird auf Deckungsebene "
            + "geführt. Dabei unterscheidet man zwischen den Komponenten Stammdeckung, "
            + "Bonus, Ansammlungsguthaben und Schlussüberschuss.";

        expectedText = "Diese Klasse enthält die Kontostände, die für die Bestimmung der abzuführenden\n" +
                "Kapitalertragssteuer gegebenenfalls benötigt werden. Sie wird auf Deckungsebene\n" +
                "geführt. Dabei unterscheidet man zwischen den Komponenten Stammdeckung, Bonus,\n" +
                "Ansammlungsguthaben und Schlussüberschuss.";
        
        wrappedText = StringUtils.wrapText(text, 80, 4, "\n");
        assertEquals(expectedText, wrappedText);
    }

}
