/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringUtilsTest {
    @Test
    public void testWrapText() {
        String text = """
                Diese Klasse enthält die Kontostände, die für die Bestimmung der abzuführenden\s
                Kapitalertragssteuer gegebenenfalls benötigt werden. Sie wird auf Deckungsebene\s
                geführt. Dabei unterscheidet man zwischen den Komponenten Stammdeckung,\s
                Bonus, Ansammlungsguthaben und Schlussüberschuss.""";//$NON-NLS-1$
        String expectedText = """
                Diese Klasse enthält die Kontostände, die für die Bestimmung der abzuführenden
                Kapitalertragssteuer gegebenenfalls benötigt werden. Sie wird auf Deckungsebene
                geführt. Dabei unterscheidet man zwischen den Komponenten Stammdeckung,\s
                Bonus, Ansammlungsguthaben und Schlussüberschuss."""; //$NON-NLS-1$

        String wrappedText = StringUtils.wrapText(text, 73, "\n"); //$NON-NLS-1$
        assertEquals(expectedText, wrappedText);

        text = """
                Diese Klasse enthält die Kontostände, die für die Bestimmung der abzuführenden \
                Kapitalertragssteuer gegebenenfalls benötigt werden. Sie wird auf Deckungsebene \
                geführt. Dabei unterscheidet man zwischen den Komponenten Stammdeckung, \
                Bonus, Ansammlungsguthaben und Schlussüberschuss."""; //$NON-NLS-1$
        expectedText = """
                Diese Klasse enthält die Kontostände, die für die Bestimmung der abzuführenden
                Kapitalertragssteuer gegebenenfalls benötigt werden. Sie wird auf Deckungsebene
                geführt. Dabei unterscheidet man zwischen den Komponenten Stammdeckung, Bonus,
                Ansammlungsguthaben und Schlussüberschuss."""; //$NON-NLS-1$

        wrappedText = StringUtils.wrapText(text, 73, "\n"); //$NON-NLS-1$
        assertEquals(expectedText, wrappedText);
    }

    @Test
    public void testComputeCopyOfName() {
        String oldName = "test"; //$NON-NLS-1$
        assertEquals(Messages.StringUtils_copyOfNamePrefix + oldName, StringUtils.computeCopyOfName(0, oldName));
        assertEquals(Messages.StringUtils_copyOfNamePrefix + "(2)_" + oldName, StringUtils.computeCopyOfName(1, //$NON-NLS-1$
                Messages.StringUtils_copyOfNamePrefix + oldName));
        assertEquals(Messages.StringUtils_copyOfNamePrefix + "(3)_" + oldName, StringUtils.computeCopyOfName(2, //$NON-NLS-1$
                Messages.StringUtils_copyOfNamePrefix + "(2)_" + oldName)); //$NON-NLS-1$
    }

    @Test
    public void testQuote() {
        assertEquals("\"xyz\"", StringUtils.quote("xyz"));
        assertEquals("\"\"", StringUtils.quote(""));
        assertEquals("\"null\"", StringUtils.quote(null));
        assertEquals("\"xyz\"", StringUtils.quote("\"xyz\""));
    }

}
