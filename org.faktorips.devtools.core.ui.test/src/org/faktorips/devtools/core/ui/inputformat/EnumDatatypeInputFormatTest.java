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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.model.plugin.EnumTypeDisplay;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumDatatypeInputFormatTest {

    @Mock
    private EnumDatatype enumDatatype;

    @Mock
    private IpsPreferences ipsPreferences;

    private EnumDatatypeInputFormat enumDatatypeInputFormat;

    @Before
    public void createInputFormat() {
        doReturn(Locale.GERMANY).when(ipsPreferences).getDatatypeFormattingLocale();
        enumDatatypeInputFormat = new EnumDatatypeInputFormat(enumDatatype, ipsPreferences);
    }

    @Before
    public void setUpEnumDatatype() {
        when(enumDatatype.isSupportingNames()).thenReturn(true);
        when(enumDatatype.getAllValueIds(false)).thenReturn(new String[] { "a", "b", "c" });
        when(enumDatatype.getValueName("a")).thenReturn("nameA");
        when(enumDatatype.getValueName("b")).thenReturn("nameB");
        when(enumDatatype.getValueName("c")).thenReturn("c (c)");
        when(enumDatatype.isParsable("a")).thenReturn(true);
        when(enumDatatype.isParsable("b")).thenReturn(true);
        when(enumDatatype.isParsable("c")).thenReturn(true);
        when(enumDatatype.isParsable("c")).thenReturn(true);
    }

    @Test
    public void testParseInternal_idConfigured_parseId() throws Exception {
        when(ipsPreferences.getEnumTypeDisplay()).thenReturn(EnumTypeDisplay.ID);

        String parsed = enumDatatypeInputFormat.parseInternal("a");

        assertEquals("a", parsed);
    }

    @Test
    public void testParseInternal_idConfigured_parseName() throws Exception {
        when(ipsPreferences.getEnumTypeDisplay()).thenReturn(EnumTypeDisplay.ID);

        String parsed = enumDatatypeInputFormat.parseInternal("nameA");

        assertEquals("a", parsed);
    }

    @Test
    public void testParseInternal_idConfigured_parseNameSpecial() throws Exception {
        when(ipsPreferences.getEnumTypeDisplay()).thenReturn(EnumTypeDisplay.ID);

        String parsed = enumDatatypeInputFormat.parseInternal("c (c)");

        assertEquals("c", parsed);
    }

    @Test
    public void testParseInternal_idConfigured_invalid() throws Exception {
        when(ipsPreferences.getEnumTypeDisplay()).thenReturn(EnumTypeDisplay.ID);

        String parsed = enumDatatypeInputFormat.parseInternal("asdsdf");

        assertEquals("asdsdf", parsed);
    }

    @Test
    public void testParseInternal_idConfigured_invalidNameAndId() throws Exception {
        when(ipsPreferences.getEnumTypeDisplay()).thenReturn(EnumTypeDisplay.ID);

        String parsed = enumDatatypeInputFormat.parseInternal("nameA (a)");

        assertEquals("nameA (a)", parsed);
    }

    @Test
    public void testParseInternal_nameConfigured_parseName() throws Exception {
        when(ipsPreferences.getEnumTypeDisplay()).thenReturn(EnumTypeDisplay.NAME);

        String parsed = enumDatatypeInputFormat.parseInternal("nameA");

        assertEquals("a", parsed);
    }

    @Test
    public void testParseInternal_nameConfigured_parseId() throws Exception {
        when(ipsPreferences.getEnumTypeDisplay()).thenReturn(EnumTypeDisplay.NAME);

        String parsed = enumDatatypeInputFormat.parseInternal("a");

        assertEquals("a", parsed);
    }

    @Test
    public void testParseInternal_nameConfigured_invalid() throws Exception {
        when(ipsPreferences.getEnumTypeDisplay()).thenReturn(EnumTypeDisplay.NAME);

        String parsed = enumDatatypeInputFormat.parseInternal("asdfsadf");

        assertEquals("asdfsadf", parsed);
    }

    @Test
    public void testParseInternal_nameConfigured_invalidNameAndId() throws Exception {
        when(ipsPreferences.getEnumTypeDisplay()).thenReturn(EnumTypeDisplay.NAME);

        String parsed = enumDatatypeInputFormat.parseInternal("nameA (a)");

        assertEquals("nameA (a)", parsed);
    }

    @Test
    public void testParseInternal_nameIdConfigured_parseNameId() throws Exception {
        when(ipsPreferences.getEnumTypeDisplay()).thenReturn(EnumTypeDisplay.NAME_AND_ID);

        String parsed = enumDatatypeInputFormat.parseInternal("nameA (a)");

        assertEquals("a", parsed);
    }

    @Test
    public void testParseInternal_nameAndidConfigured_parseNameSpecial() throws Exception {
        when(ipsPreferences.getEnumTypeDisplay()).thenReturn(EnumTypeDisplay.NAME_AND_ID);

        String parsed = enumDatatypeInputFormat.parseInternal("c (c) (c)");

        assertEquals("c", parsed);
    }

    @Test
    public void testParseInternal_nameIdConfigured_parseId() throws Exception {
        when(ipsPreferences.getEnumTypeDisplay()).thenReturn(EnumTypeDisplay.NAME_AND_ID);

        String parsed = enumDatatypeInputFormat.parseInternal("a");

        assertEquals("a", parsed);
    }

    @Test
    public void testParseInternal_nameIdConfigured_parseName() throws Exception {
        when(ipsPreferences.getEnumTypeDisplay()).thenReturn(EnumTypeDisplay.NAME_AND_ID);

        String parsed = enumDatatypeInputFormat.parseInternal("nameA");

        assertEquals("a", parsed);
    }

    @Test
    public void testParseInternal_nameIdConfigured_invalid() throws Exception {
        when(ipsPreferences.getEnumTypeDisplay()).thenReturn(EnumTypeDisplay.NAME_AND_ID);

        String parsed = enumDatatypeInputFormat.parseInternal("asdfdsaf");

        assertEquals("asdfdsaf", parsed);
    }

    @Test
    public void testParseValueName_findName() throws Exception {
        String parseValueName = enumDatatypeInputFormat.parseValueName("nameA");

        assertEquals("a", parseValueName);
    }

    @Test
    public void testParseValueName_invalidName() throws Exception {
        String parseValueName = enumDatatypeInputFormat.parseValueName("xasd");

        assertNull(parseValueName);
    }

    @Test
    public void testParseValueNameAndID() throws Exception {
        String parseValueName = enumDatatypeInputFormat.parseValueNameAndID("nameA (a)");

        assertEquals("a", parseValueName);
    }

    @Test
    public void testParseValueNameAndID_withParentheses() throws Exception {
        String parseValueName = enumDatatypeInputFormat.parseValueNameAndID("nameA (xyz) (a)");

        assertEquals("a", parseValueName);
    }

    /*
     * Ensures that a string that violates the convention cannot be parsed. The pattern/convention
     * is "<enumValue-Name>(<enumValue-ID>)". The name can contain nested parentheses, the id can
     * not.
     */
    @Test
    public void testCannotParse_whenClosingParanthesisNotFollowedByStringEnding() throws Exception {
        String parseValueName = enumDatatypeInputFormat.parseValueNameAndID("nameA(x(y)z)(a) ");

        assertNull(parseValueName);
    }

    /*
     * Ensures that a string that violates the convention cannot be parsed. The pattern/convention
     * is "<enumValue-Name>(<enumValue-ID>)". The name can contain nested parentheses, the id can
     * not.
     */
    @Test
    public void testCannotParse_whenIdContainsNestedParantheses() throws Exception {
        String parseValueName = enumDatatypeInputFormat.parseValueNameAndID("nameA(x(y)z)(a(b)c)");

        assertNull(parseValueName);
    }

    @Test
    public void testParseValueNameAndID_invalidName() throws Exception {
        String parseValueName = enumDatatypeInputFormat.parseValueNameAndID("asdsg (agds)");

        assertNull(parseValueName);
    }

    @Test
    public void testParseValueNameAndID_notNameAndId() throws Exception {
        String parseValueName = enumDatatypeInputFormat.parseValueNameAndID("agds (");

        assertNull(parseValueName);
    }

    @Test
    public void testFormatNullIsEmptyString() throws Exception {
        String nullString = enumDatatypeInputFormat.format(null);

        assertEquals(StringUtils.EMPTY, nullString);
    }

}
