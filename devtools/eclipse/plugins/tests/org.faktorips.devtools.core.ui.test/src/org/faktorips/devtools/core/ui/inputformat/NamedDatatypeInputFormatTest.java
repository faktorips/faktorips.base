/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.inputformat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.model.plugin.NamedDataTypeDisplay;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NamedDatatypeInputFormatTest {

    @Mock
    private EnumDatatype enumDatatype;

    @Mock
    private IpsPreferences ipsPreferences;

    private NamedDatatypeInputFormat NamedDatatypeInputFormat;

    @Before
    public void createInputFormat() {
        doReturn(Locale.GERMANY).when(ipsPreferences).getDatatypeFormattingLocale();
        NamedDatatypeInputFormat = new NamedDatatypeInputFormat(enumDatatype, ipsPreferences);
    }

    @Before
    public void setUpEnumDatatype() {

        when(enumDatatype.getValueByName(eq("nameA"), any(Locale.class))).thenReturn("a");
        when(enumDatatype.valueToString("a")).thenReturn("a");
        when(enumDatatype.getValueByName(eq("c (c)"), any(Locale.class))).thenReturn("c");
        when(enumDatatype.valueToString("c")).thenReturn("c");

        when(enumDatatype.isParsable("a")).thenReturn(true);
        when(enumDatatype.isParsable("c")).thenReturn(true);
    }

    @Test
    public void testParseInternal_idConfigured_parseId() throws Exception {
        String parsed = NamedDatatypeInputFormat.parseInternal("a");

        assertEquals("a", parsed);
    }

    @Test
    public void testParseInternal_idConfigured_parseName() throws Exception {
        String parsed = NamedDatatypeInputFormat.parseInternal("nameA");

        assertEquals("a", parsed);
    }

    @Test
    public void testParseInternal_idConfigured_parseNameSpecial() throws Exception {
        String parsed = NamedDatatypeInputFormat.parseInternal("c (c)");

        assertEquals("c", parsed);
    }

    @Test
    public void testParseInternal_idConfigured_invalid() throws Exception {
        when(ipsPreferences.getNamedDataTypeDisplay()).thenReturn(NamedDataTypeDisplay.ID);

        String parsed = NamedDatatypeInputFormat.parseInternal("asdsdf");

        assertEquals("asdsdf", parsed);
    }

    @Test
    public void testParseInternal_idConfigured_invalidNameAndId() throws Exception {
        when(ipsPreferences.getNamedDataTypeDisplay()).thenReturn(NamedDataTypeDisplay.ID);

        String parsed = NamedDatatypeInputFormat.parseInternal("nameA (a)");

        assertEquals("nameA (a)", parsed);
    }

    @Test
    public void testParseInternal_nameConfigured_parseName() throws Exception {
        String parsed = NamedDatatypeInputFormat.parseInternal("nameA");

        assertEquals("a", parsed);
    }

    @Test
    public void testParseInternal_nameConfigured_parseId() throws Exception {
        String parsed = NamedDatatypeInputFormat.parseInternal("a");

        assertEquals("a", parsed);
    }

    @Test
    public void testParseInternal_nameConfigured_invalid() throws Exception {
        when(ipsPreferences.getNamedDataTypeDisplay()).thenReturn(NamedDataTypeDisplay.NAME);

        String parsed = NamedDatatypeInputFormat.parseInternal("asdfsadf");

        assertEquals("asdfsadf", parsed);
    }

    @Test
    public void testParseInternal_nameConfigured_invalidNameAndId() throws Exception {
        when(ipsPreferences.getNamedDataTypeDisplay()).thenReturn(NamedDataTypeDisplay.NAME);

        String parsed = NamedDatatypeInputFormat.parseInternal("nameA (a)");

        assertEquals("nameA (a)", parsed);
    }

    @Test
    public void testParseInternal_nameIdConfigured_parseNameId() throws Exception {
        when(ipsPreferences.getNamedDataTypeDisplay()).thenReturn(NamedDataTypeDisplay.NAME_AND_ID);

        String parsed = NamedDatatypeInputFormat.parseInternal("nameA (a)");

        assertEquals("a", parsed);
    }

    @Test
    public void testParseInternal_nameAndidConfigured_parseNameSpecial() throws Exception {
        when(ipsPreferences.getNamedDataTypeDisplay()).thenReturn(NamedDataTypeDisplay.NAME_AND_ID);

        String parsed = NamedDatatypeInputFormat.parseInternal("c (c) (c)");

        assertEquals("c", parsed);
    }

    @Test
    public void testParseInternal_nameIdConfigured_parseId() throws Exception {
        String parsed = NamedDatatypeInputFormat.parseInternal("a");

        assertEquals("a", parsed);
    }

    @Test
    public void testParseInternal_nameIdConfigured_parseName() throws Exception {
        String parsed = NamedDatatypeInputFormat.parseInternal("nameA");

        assertEquals("a", parsed);
    }

    @Test
    public void testParseInternal_nameIdConfigured_invalid() throws Exception {
        when(ipsPreferences.getNamedDataTypeDisplay()).thenReturn(NamedDataTypeDisplay.NAME_AND_ID);

        String parsed = NamedDatatypeInputFormat.parseInternal("asdfdsaf");

        assertEquals("asdfdsaf", parsed);
    }

    @Test
    public void testParseValueName_findName() throws Exception {
        String parseValueName = NamedDatatypeInputFormat.parseValueName("nameA");

        assertEquals("a", parseValueName);
    }

    @Test
    public void testParseValueName_invalidName() throws Exception {
        String parseValueName = NamedDatatypeInputFormat.parseValueName("xasd");

        assertNull(parseValueName);
    }

    @Test
    public void testParseValueNameAndID() throws Exception {
        String parseValueName = NamedDatatypeInputFormat.parseValueNameAndID("nameA (a)");

        assertEquals("a", parseValueName);
    }

    @Test
    public void testParseValueNameAndID_withParentheses() throws Exception {
        String parseValueName = NamedDatatypeInputFormat.parseValueNameAndID("nameA (xyz) (a)");

        assertEquals("a", parseValueName);
    }

    /*
     * Ensures that a string that violates the convention cannot be parsed. The pattern/convention
     * is "<enumValue-Name>(<enumValue-ID>)". The name can contain nested parentheses, the id can
     * not.
     */
    @Test
    public void testCannotParse_whenClosingParanthesisNotFollowedByStringEnding() throws Exception {
        String parseValueName = NamedDatatypeInputFormat.parseValueNameAndID("nameA(x(y)z)(a) ");

        assertNull(parseValueName);
    }

    /*
     * Ensures that a string that violates the convention cannot be parsed. The pattern/convention
     * is "<enumValue-Name>(<enumValue-ID>)". The name can contain nested parentheses, the id can
     * not.
     */
    @Test
    public void testCannotParse_whenIdContainsNestedParantheses() throws Exception {
        String parseValueName = NamedDatatypeInputFormat.parseValueNameAndID("nameA(x(y)z)(a(b)c)");

        assertNull(parseValueName);
    }

    @Test
    public void testParseValueNameAndID_invalidName() throws Exception {
        String parseValueName = NamedDatatypeInputFormat.parseValueNameAndID("asdsg (agds)");

        assertNull(parseValueName);
    }

    @Test
    public void testParseValueNameAndID_notNameAndId() throws Exception {
        String parseValueName = NamedDatatypeInputFormat.parseValueNameAndID("agds (");

        assertNull(parseValueName);
    }

    @Test
    public void testFormatNullIsEmptyString() throws Exception {
        String nullString = NamedDatatypeInputFormat.format(null);

        assertEquals(IpsStringUtils.EMPTY, nullString);
    }

}
