/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringUtilsTest {
    @Test
    public void testWrapText() {
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

    @Test
    public void testComputeCopyOfName() {
        String oldName = "test"; //$NON-NLS-1$
        assertEquals(Messages.StringUtils_copyOfNamePrefix + oldName, StringUtils.computeCopyOfName(0, oldName));
        assertEquals(Messages.StringUtils_copyOfNamePrefix + "(2)_" + oldName, StringUtils.computeCopyOfName(1, //$NON-NLS-1$
                Messages.StringUtils_copyOfNamePrefix + oldName));
        assertEquals(Messages.StringUtils_copyOfNamePrefix + "(3)_" + oldName, StringUtils.computeCopyOfName(2, //$NON-NLS-1$
                Messages.StringUtils_copyOfNamePrefix + "(2)_" + oldName)); //$NON-NLS-1$
    }

}
