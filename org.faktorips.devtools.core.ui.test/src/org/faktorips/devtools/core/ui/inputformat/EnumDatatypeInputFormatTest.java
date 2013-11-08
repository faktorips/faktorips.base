/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.inputformat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.EnumTypeDisplay;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.ui.inputformat.EnumDatatypeInputFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumDatatypeInputFormatTest {

    @Mock
    private EnumDatatype enumDatatype;

    @Mock
    private IpsPreferences ipsPreferences;

    @InjectMocks
    private EnumDatatypeInputFormat enumDatatypeInputFormat;

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
    public void testParseValueNameAndID_invalidName() throws Exception {
        String parseValueName = enumDatatypeInputFormat.parseValueNameAndID("asdsg (agds)");

        assertNull(parseValueName);
    }

    @Test
    public void testParseValueNameAndID_notNameAndId() throws Exception {
        String parseValueName = enumDatatypeInputFormat.parseValueNameAndID("agds");

        assertNull(parseValueName);
    }

}
