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

        String text = "Diese Klasse enth�lt die Kontost�nde, die f�r die Bestimmung der abzuf�hrenden \n"
                + "Kapitalertragssteuer gegebenenfalls ben�tigt werden. Sie wird auf Deckungsebene \n"
                + "gef�hrt. Dabei unterscheidet man zwischen den Komponenten Stammdeckung, \n"
                + "Bonus, Ansammlungsguthaben und Schluss�berschuss.";

        String expectedText = "Diese Klasse enth�lt die Kontost�nde, die f�r die Bestimmung der abzuf�hrenden\n"
                + "Kapitalertragssteuer gegebenenfalls ben�tigt werden. Sie wird auf Deckungsebene\n"
                + "gef�hrt. Dabei unterscheidet man zwischen den Komponenten Stammdeckung, \n"
                + "Bonus, Ansammlungsguthaben und Schluss�berschuss.";

        String wrappedText = StringUtils.wrapText(text, 73, "\n");
        assertEquals(expectedText, wrappedText);

        text = "Diese Klasse enth�lt die Kontost�nde, die f�r die Bestimmung der abzuf�hrenden "
                + "Kapitalertragssteuer gegebenenfalls ben�tigt werden. Sie wird auf Deckungsebene "
                + "gef�hrt. Dabei unterscheidet man zwischen den Komponenten Stammdeckung, "
                + "Bonus, Ansammlungsguthaben und Schluss�berschuss.";

        expectedText = "Diese Klasse enth�lt die Kontost�nde, die f�r die Bestimmung der abzuf�hrenden\n"
                + "Kapitalertragssteuer gegebenenfalls ben�tigt werden. Sie wird auf Deckungsebene\n"
                + "gef�hrt. Dabei unterscheidet man zwischen den Komponenten Stammdeckung, Bonus,\n"
                + "Ansammlungsguthaben und Schluss�berschuss.";

        wrappedText = StringUtils.wrapText(text, 73, "\n");
        assertEquals(expectedText, wrappedText);
    }

    public void testComputeCopyOfName() {
        String oldName = "test";
        assertEquals(Messages.StringUtils_copyOfNamePrefix + oldName, StringUtils.computeCopyOfName(0, oldName));
        assertEquals(Messages.StringUtils_copyOfNamePrefix + "(2)_" + oldName, StringUtils.computeCopyOfName(1,
                Messages.StringUtils_copyOfNamePrefix + oldName));
        assertEquals(Messages.StringUtils_copyOfNamePrefix + "(3)_" + oldName, StringUtils.computeCopyOfName(2,
                Messages.StringUtils_copyOfNamePrefix + "(2)_" + oldName));
    }
}
