/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class OsgiVersionFormatTest {

    private OsgiVersionFormat format = new OsgiVersionFormat();

    @Test
    public void testIsCorrectVersionFormat_ValidFormOnlyDigits() {
        OsgiVersion osgiVersion = new OsgiVersion("3.9");
        boolean correctFormat = format.isCorrectVersionFormat(osgiVersion.asString());
        assertTrue(correctFormat);
    }

    @Test
    public void testIsCorrectVersionFormat_ValidFormDigitsAndLetters() {
        OsgiVersion osgiVersion = new OsgiVersion("0.6.4.lal");
        boolean correctFormat = format.isCorrectVersionFormat(osgiVersion.asString());
        assertTrue(correctFormat);
    }

    @Test(expected = NumberFormatException.class)
    public void testIsCorrectVersionFormat_Invalid() {
        OsgiVersion osgiVersion = new OsgiVersion("lal");
        boolean correctFormat = format.isCorrectVersionFormat(osgiVersion.asString());
        assertFalse(correctFormat);
    }
}
