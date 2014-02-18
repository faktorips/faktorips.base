/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.refactor;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class TextRegionTest {

    private String completeIdentifierString = "oldString.oldPartOfString";
    private String newString = "thisIsTheNew";
    private TextRegion region;

    @Test
    public void testCreateFullRefactoredString() {
        region = new TextRegion(0, 3);
        String refactoredString = region.createFullRefactoredString(completeIdentifierString, newString);

        assertEquals("thisIsTheNewString.oldPartOfString", refactoredString);
    }

    @Test
    public void testCreateFullRefactoredStringEmptyString() {
        region = new TextRegion(10, 17);
        String refactoredString = region.createFullRefactoredString(completeIdentifierString, StringUtils.EMPTY);

        assertEquals("oldString.OfString", refactoredString);
    }

    @Test
    public void testCreateFullRefactoredStringInvalidStartEndPoints() {
        region = new TextRegion(-1, -8);
        String refactoredString = region.createFullRefactoredString(completeIdentifierString, StringUtils.EMPTY);

        assertEquals(completeIdentifierString, refactoredString);
    }

    @Test
    public void testOffset() throws Exception {
        TextRegion textRegion = new TextRegion(13, 57);

        TextRegion offsetTextRegion = textRegion.offset(42);

        assertEquals(55, offsetTextRegion.getStartPoint());
        assertEquals(99, offsetTextRegion.getEndPoint());
    }

    @Test
    public void testStartOffset() throws Exception {
        TextRegion textRegion = new TextRegion(13, 57);

        TextRegion offsetTextRegion = textRegion.startOffset(42);

        assertEquals(55, offsetTextRegion.getStartPoint());
        assertEquals(57, offsetTextRegion.getEndPoint());
    }

    @Test
    public void testEndOffset() throws Exception {
        TextRegion textRegion = new TextRegion(13, 57);

        TextRegion offsetTextRegion = textRegion.endOffset(42);

        assertEquals(13, offsetTextRegion.getStartPoint());
        assertEquals(99, offsetTextRegion.getEndPoint());
    }

}
