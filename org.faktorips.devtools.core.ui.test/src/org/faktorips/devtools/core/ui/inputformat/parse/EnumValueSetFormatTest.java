/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.inputformat.parse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.inputformat.DefaultInputFormat;
import org.faktorips.devtools.core.ui.inputformat.IntegerNumberFormat;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.productcmpt.ConfiguredValueSet;
import org.faktorips.devtools.model.internal.valueset.EnumValueSet;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumValueSetFormatTest {

    @Mock
    private IIpsObject ipsObject;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IIpsModel ipsModel;

    @Mock
    private IpsUIPlugin uiPlugin;

    @Mock
    private ConfiguredValueSet configValueSet;

    @Mock
    private ValueDatatype datatype;

    private IEnumValueSet enumValueSet;

    private AbstractValueSetFormat enumVSFormat;

    @Before
    public void setUp() throws Exception {
        enumVSFormat = new EnumValueSetFormat(configValueSet, uiPlugin);
        enumValueSet = new EnumValueSet(configValueSet, "");

        when(uiPlugin.getInputFormat(Mockito.any(ValueDatatype.class), Mockito.any(IIpsProject.class))).thenReturn(
                new DefaultInputFormat(null));
        when(configValueSet.findValueDatatype(ipsProject)).thenReturn(datatype);
        when(configValueSet.getIpsProject()).thenReturn(ipsProject);
        when(configValueSet.getIpsModel()).thenReturn(ipsModel);
        when(configValueSet.getIpsObject()).thenReturn(ipsObject);
        when(configValueSet.getValueSet()).thenReturn(enumValueSet);
    }

    @Test
    public void testParseInternalNewEnumValueSet() throws Exception {
        IValueSet valueSet = enumVSFormat.parse("test | test2");
        enumValueSet.addValue("test | test1");
        IEnumValueSet enumVS = (IEnumValueSet)valueSet;

        assertNotNull(valueSet);
        assertTrue(valueSet instanceof EnumValueSet);
        assertEquals(configValueSet, enumVS.getParent());
        assertEquals(2, enumVS.getValuesAsList().size());
        assertEquals("test", enumVS.getValue(0));
        assertEquals("test2", enumVS.getValue(1));
    }

    @Test
    public void testParseInternalOldEnumValueSet() throws Exception {
        enumValueSet.addValue("test");
        enumValueSet.addValue("test1");
        IEnumValueSet enumVS = (IEnumValueSet)enumVSFormat.parse("test | test1");

        assertNotNull(enumVSFormat.parse("test | test1"));
        assertTrue(enumVSFormat.parse("test | test1") instanceof EnumValueSet);
        assertEquals(configValueSet, enumVS.getParent());
        assertTrue(("").equals(enumVSFormat.parse("test | test1").getId()));
        assertEquals(2, enumVS.getValuesAsList().size());
        assertEquals("test", enumVS.getValue(0));
        assertEquals("test1", enumVS.getValue(1));
    }

    @Test
    public void testParseInternal_ReturnValueBlankIfTextBlank() throws Exception {
        enumValueSet.addValue("test");
        enumValueSet.addValue("test1");
        IEnumValueSet enumVS = (IEnumValueSet)enumVSFormat.parse("");

        assertNotNull(enumVSFormat.parse(""));
        assertTrue(enumVSFormat.parse("") instanceof EnumValueSet);
        assertEquals(configValueSet, enumVS.getParent());
        assertEquals(1, enumVS.getValuesAsList().size());
        assertEquals("", enumVS.getValuesAsList().get(0));
    }

    @Test
    public void testParseInternal_ReturnEmptyValueListIfInvalidText() throws Exception {
        when(uiPlugin.getInputFormat(Mockito.any(ValueDatatype.class), Mockito.any(IIpsProject.class))).thenReturn(
                IntegerNumberFormat.newInstance(null));
        enumValueSet.addValue("test");
        enumValueSet.addValue("test1");
        IEnumValueSet enumVS = (IEnumValueSet)enumVSFormat.parse("");

        assertNotNull(enumVSFormat.parse(""));
        assertTrue(enumVSFormat.parse("") instanceof EnumValueSet);
        assertEquals(configValueSet, enumVS.getParent());
        assertEquals(0, enumVS.getValuesAsList().size());
    }

    @Test
    public void testParseInternal_ReturnValueNullIfTextNullPresentation() throws Exception {
        enumValueSet.addValue("test");
        enumValueSet.addValue("test1");
        IEnumValueSet enumVS = (IEnumValueSet)enumVSFormat.parse(IpsPlugin.getDefault().getIpsPreferences()
                .getNullPresentation());

        assertNotNull(enumVSFormat.parse(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation()));
        assertTrue(enumVSFormat
                .parse(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation()) instanceof EnumValueSet);
        assertEquals(configValueSet, enumVS.getParent());
        assertEquals(1, enumVS.getValuesAsList().size());
        assertEquals(null, enumVS.getValue(0));
    }

    @Test
    public void testParseInternal_() throws Exception {
        enumValueSet.addValue("test");
        enumValueSet.addValue("test1");
        IEnumValueSet enumVS = (IEnumValueSet)enumVSFormat.parse(IpsPlugin.getDefault().getIpsPreferences()
                .getNullPresentation());

        assertNotNull(enumVSFormat.parse(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation()));
        assertTrue(enumVSFormat
                .parse(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation()) instanceof EnumValueSet);
        assertEquals(configValueSet, enumVS.getParent());
        assertEquals(1, enumVS.getValuesAsList().size());
        assertEquals(null, enumVS.getValue(0));
    }

    @Test
    public void testFormatInternal_EmptyStringInFront() {
        enumValueSet.addValue("");
        enumValueSet.addValue("A");
        enumValueSet.addValue("B");

        String formatted = ((EnumValueSetFormat)enumVSFormat).formatInternal(enumValueSet);

        assertEquals(" | A | B", formatted);
    }

    @Test
    public void testFormatInternal_EmptyStringInMiddle() {
        enumValueSet.addValue("A");
        enumValueSet.addValue("");
        enumValueSet.addValue("B");

        String formatted = ((EnumValueSetFormat)enumVSFormat).formatInternal(enumValueSet);

        assertEquals("A |  | B", formatted);
    }

    @Test
    public void testFormatInternal_EmptyStringOnEnd() {
        enumValueSet.addValue("A");
        enumValueSet.addValue("B");
        enumValueSet.addValue("");

        String formatted = ((EnumValueSetFormat)enumVSFormat).formatInternal(enumValueSet);

        assertEquals("A | B | ", formatted);
    }

    @Test
    public void testFormatInternal_EnumValueSet_Empty() {
        String formatted = ((EnumValueSetFormat)enumVSFormat).formatInternal(enumValueSet);

        assertEquals("{}", formatted);
    }

    @Test
    public void testIsResponsibleFor_ReturnTrueIfOnlyEnumValeSetTypeIsAllowed() throws CoreRuntimeException {
        when(configValueSet.getAllowedValueSetTypes(ipsProject)).thenReturn(Arrays.asList(ValueSetType.ENUM));

        assertTrue(enumVSFormat.isResponsibleFor("test | test1"));
    }

    @Test
    public void testIsResponsibleFor_ReturnTrueIfEnumValeSetTypeIsAllowedAndTextLooksLikeEnum() throws CoreRuntimeException {
        when(configValueSet.getAllowedValueSetTypes(ipsProject)).thenReturn(
                Arrays.asList(ValueSetType.ENUM, ValueSetType.UNRESTRICTED));

        assertTrue(enumVSFormat.isResponsibleFor("test | test1"));
    }

    @Test
    public void testIsResponsibleFor_ReturnTrueIfEnumValeSetTypeIsAllowedAndTextLooksLikeEnum_EmptyString()
            throws CoreRuntimeException {
        when(configValueSet.getAllowedValueSetTypes(ipsProject)).thenReturn(
                Arrays.asList(ValueSetType.ENUM, ValueSetType.UNRESTRICTED));

        assertTrue(enumVSFormat.isResponsibleFor(""));
    }
}
