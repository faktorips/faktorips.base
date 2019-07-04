/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder.flidentifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierParser.IdentifierMatcher;
import org.junit.Test;

public class IdentifierMatcherTest {

    private static final String MY_PARAMETER = "anyParameter";

    private static final String MY_ATTRIBUTE = "myAttribute";

    private static final String MY_QUALIFIER = "abc123";

    private IdentifierMatcher identifierMatcher;

    private String stringWithThreeIdentifier() {
        String inputString = MY_PARAMETER + '.' + MY_ATTRIBUTE + '.' + MY_QUALIFIER;
        identifierMatcher = new IdentifierMatcher(inputString);
        return inputString;
    }

    @Test
    public void testIdentifierMatcherTextRegionSetRightFirstIdentifier() {
        stringWithThreeIdentifier();

        assertEquals(0, identifierMatcher.getTextRegion().getStart());
        assertEquals(MY_PARAMETER.length(), identifierMatcher.getTextRegion().getEnd());
    }

    @Test
    public void testIdentifierMatcherTextRegionSetRightSecondIdentifier() {
        stringWithThreeIdentifier();
        identifierMatcher.nextIdentifierPart();

        assertEquals(MY_PARAMETER.length() + 1, identifierMatcher.getTextRegion().getStart());
        assertEquals((MY_PARAMETER + '.' + MY_ATTRIBUTE).length(), identifierMatcher.getTextRegion().getEnd());

    }

    @Test
    public void testIdentifierMatcherTextRegionSetRightThirdIdentifier() {
        String inputString = stringWithThreeIdentifier();
        identifierMatcher.nextIdentifierPart();
        identifierMatcher.nextIdentifierPart();

        assertEquals((MY_PARAMETER + '.' + MY_ATTRIBUTE).length() + 1, identifierMatcher.getTextRegion().getStart());
        assertEquals(inputString.length(), identifierMatcher.getTextRegion().getEnd());
    }

    @Test
    public void testIdentifierMatcherGetIdentifierPart() {
        identifierMatcher = new IdentifierMatcher(MY_PARAMETER + '.' + MY_ATTRIBUTE);
        String identifierPartMyParameter = identifierMatcher.getIdentifierPart();
        identifierMatcher.nextIdentifierPart();
        String identifierPartMyAttribute = identifierMatcher.getIdentifierPart();

        assertEquals(MY_PARAMETER, identifierPartMyParameter);
        assertEquals(MY_ATTRIBUTE, identifierPartMyAttribute);

    }

    @Test
    public void testIdentifierMatcherHasNextIdentifierPart() {
        identifierMatcher = new IdentifierMatcher(MY_PARAMETER + '.' + MY_ATTRIBUTE);
        boolean myPrarameterHasNext = identifierMatcher.hasNextIdentifierPart();
        identifierMatcher.nextIdentifierPart();
        boolean myAttributeHasNext = identifierMatcher.hasNextIdentifierPart();

        assertTrue(myPrarameterHasNext);
        assertFalse(myAttributeHasNext);
    }

    @Test
    public void testIdentifierMatcherNoNextIdentifierPart() {
        identifierMatcher = new IdentifierMatcher(MY_PARAMETER);

        assertFalse(identifierMatcher.hasNextIdentifierPart());
    }

    @Test
    public void testIdentifierMatcherEmptyIdentifier() {
        identifierMatcher = new IdentifierMatcher(StringUtils.EMPTY);

        assertEquals(identifierMatcher.getTextRegion().getStart(), identifierMatcher.getTextRegion().getEnd());
        assertFalse(identifierMatcher.hasNextIdentifierPart());
    }

}
