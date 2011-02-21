/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.util;

import junit.framework.TestCase;

public class StringUtilsTest extends TestCase {

    public final void testWrapText() {
        String text = "Diese Klasse enthält die Kontostände, die für die Bestimmung der abzuführenden \n" //$NON-NLS-1$
                + "Kapitalertragssteuer gegebenenfalls benötigt werden. Sie wird auf Deckungsebene \n" //$NON-NLS-1$
                + "geführt. Dabei unterscheidet man zwischen den Komponenten Stammdeckung, \n" //$NON-NLS-1$
                + "Bonus, Ansammlungsguthaben und Schlussüberschuss."; //$NON-NLS-1$

        String expectedText = "Diese Klasse enthält die Kontostände, die für die Bestimmung der abzuführenden\n" //$NON-NLS-1$
                + "Kapitalertragssteuer gegebenenfalls benötigt werden. Sie wird auf Deckungsebene\n" //$NON-NLS-1$
                + "geführt. Dabei unterscheidet man zwischen den Komponenten Stammdeckung, \n" //$NON-NLS-1$
                + "Bonus, Ansammlungsguthaben und Schlussüberschuss."; //$NON-NLS-1$

        String wrappedText = StringUtils.wrapText(text, 73, "\n"); //$NON-NLS-1$
        assertEquals(expectedText, wrappedText);

        text = "Diese Klasse enthält die Kontostände, die für die Bestimmung der abzuführenden " //$NON-NLS-1$
                + "Kapitalertragssteuer gegebenenfalls benötigt werden. Sie wird auf Deckungsebene " //$NON-NLS-1$
                + "geführt. Dabei unterscheidet man zwischen den Komponenten Stammdeckung, " //$NON-NLS-1$
                + "Bonus, Ansammlungsguthaben und Schlussüberschuss."; //$NON-NLS-1$

        expectedText = "Diese Klasse enthält die Kontostände, die für die Bestimmung der abzuführenden\n" //$NON-NLS-1$
                + "Kapitalertragssteuer gegebenenfalls benötigt werden. Sie wird auf Deckungsebene\n" //$NON-NLS-1$
                + "geführt. Dabei unterscheidet man zwischen den Komponenten Stammdeckung, Bonus,\n" //$NON-NLS-1$
                + "Ansammlungsguthaben und Schlussüberschuss."; //$NON-NLS-1$

        wrappedText = StringUtils.wrapText(text, 73, "\n"); //$NON-NLS-1$
        assertEquals(expectedText, wrappedText);
    }

    public void testComputeCopyOfName() {
        String oldName = "test"; //$NON-NLS-1$
        assertEquals(Messages.StringUtils_copyOfNamePrefix + oldName, StringUtils.computeCopyOfName(0, oldName));
        assertEquals(Messages.StringUtils_copyOfNamePrefix + "(2)_" + oldName, StringUtils.computeCopyOfName(1, //$NON-NLS-1$
                Messages.StringUtils_copyOfNamePrefix + oldName));
        assertEquals(Messages.StringUtils_copyOfNamePrefix + "(3)_" + oldName, StringUtils.computeCopyOfName(2, //$NON-NLS-1$
                Messages.StringUtils_copyOfNamePrefix + "(2)_" + oldName)); //$NON-NLS-1$
    }

}
