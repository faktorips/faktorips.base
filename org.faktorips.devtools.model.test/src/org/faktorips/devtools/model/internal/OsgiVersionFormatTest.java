/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class OsgiVersionFormatTest {

    private OsgiVersionFormat format = new OsgiVersionFormat();

    @Test
    public void testIsCorrectVersionFormat_ValidFormOneDigits() {
        boolean correctFormat = format.isCorrectVersionFormat("3");

        assertTrue(correctFormat);
    }

    @Test
    public void testIsCorrectVersionFormat_ValidFormTwoDigits() {
        boolean correctFormat = format.isCorrectVersionFormat("3.9");

        assertTrue(correctFormat);
    }

    @Test
    public void testIsCorrectVersionFormat_ValidFormTheeDigits() {
        boolean correctFormat = format.isCorrectVersionFormat("3.2.5");

        assertTrue(correctFormat);
    }

    @Test
    public void testIsCorrectVersionFormat_ValidFormDigitsAndLetters() {
        boolean correctFormat = format.isCorrectVersionFormat("0.6.4.lal");

        assertTrue(correctFormat);
    }

    @Test
    public void testIsCorrectVersionFormat_Invalid() {
        boolean correctFormat = format.isCorrectVersionFormat("lal");

        assertFalse(correctFormat);
    }

}
