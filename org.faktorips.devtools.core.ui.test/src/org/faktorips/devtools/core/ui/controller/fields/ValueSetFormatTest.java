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

package org.faktorips.devtools.core.ui.controller.fields;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ValueSetFormatTest {

    @Mock
    private IConfigElement configElement;

    @Mock
    private IpsUIPlugin uiPlugin;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private List<ValueSetType> listAllowdValueTypes;

    @Mock
    private IConfigElement parent;

    @Mock
    private ValueDatatype datatype;

    @Mock
    private IInputFormat<String> inputFormat;

    private String INPUT_VALUE = "test.input";

    private String OUTPUT_VALUE = "test.output";

    private EnumValueSet enumValueSet;

    private ValueSetFormat format;

    @Before
    public void setUp() throws Exception {
        enumValueSet = new EnumValueSet(parent, "ID");
        when(configElement.getValueSet()).thenReturn(enumValueSet);
        when(configElement.getIpsProject()).thenReturn(ipsProject);
        when(configElement.getAllowedValueSetTypes(ipsProject)).thenReturn(listAllowdValueTypes);
        when(listAllowdValueTypes.contains(ValueSetType.UNRESTRICTED)).thenReturn(false);
        format = new ValueSetFormat(configElement, uiPlugin);
    }

    @Test
    public void testParseInternalEmptyUnrestrictedValueSet() throws Exception {
        when(listAllowdValueTypes.contains(ValueSetType.UNRESTRICTED)).thenReturn(true);

        IValueSet parseInternal = format.parseInternal("");

        assertNotNull(parseInternal);
        assertTrue(parseInternal instanceof UnrestrictedValueSet);
        assertEquals(parent, parseInternal.getParent());
        assertEquals("ID", parseInternal.getId());
    }

    @Test
    public void testParseInternalEmptyEnumValueSet() {
        IValueSet parseInternal = format.parseInternal("");

        assertNotNull(parseInternal);
        assertTrue(parseInternal instanceof EnumValueSet);
        assertEquals(parent, parseInternal.getParent());
        assertFalse(parseInternal.getId().equals("ID"));
    }

    @Test
    public void testParseInternalUnrestrictedValueSet() throws Exception {
        when(listAllowdValueTypes.contains(ValueSetType.UNRESTRICTED)).thenReturn(true);

        IValueSet parseInternal = format.parseInternal("<unrestriced>");

        assertNotNull(parseInternal);
        assertTrue(parseInternal instanceof UnrestrictedValueSet);
        assertEquals(parent, parseInternal.getParent());
        assertEquals("ID", parseInternal.getId());
    }

    @Test
    public void testParseInternalNewEnumValueSet() {
        IValueSet parseInternal = format.parseInternal("test | test2");
        enumValueSet.addValueWithoutTriggeringChangeEvent("test | test1");

        assertNotNull(parseInternal);
        assertTrue(parseInternal instanceof EnumValueSet);
        EnumValueSet enumVS = (EnumValueSet)parseInternal;
        assertEquals(parent, enumVS.getParent());
        assertFalse(parseInternal.getId().equals("ID"));
        assertEquals(2, enumVS.getValuesAsList().size());
        assertEquals("test", enumVS.getValue(0));
        assertEquals("test2", enumVS.getValue(1));
    }

    @Test
    public void testParseWithFormatter_InputFormatNotNull() throws CoreException {
        when(configElement.findValueDatatype(ipsProject)).thenReturn(datatype);
        when(uiPlugin.getInputFormat(datatype)).thenReturn(inputFormat);
        when(inputFormat.parse(INPUT_VALUE)).thenReturn(OUTPUT_VALUE);
        String result = format.parseWithFormater(INPUT_VALUE);
        assertEquals(OUTPUT_VALUE, result);
    }

    @Test
    public void testParseWithFormatter_InputFormatNull() {
        String result = format.parseWithFormater(INPUT_VALUE);
        assertEquals(INPUT_VALUE, result);
    }

    @Test
    public void testParseInternalOldEnumValueSet() {
        enumValueSet.addValueWithoutTriggeringChangeEvent("test");
        enumValueSet.addValueWithoutTriggeringChangeEvent("test1");
        IValueSet parseInternal = format.parseInternal("test | test1");

        assertNotNull(parseInternal);
        assertTrue(parseInternal instanceof EnumValueSet);
        EnumValueSet enumVS = (EnumValueSet)parseInternal;
        assertEquals(parent, enumVS.getParent());
        assertTrue(parseInternal.getId().equals("ID"));
        assertEquals(2, enumVS.getValuesAsList().size());
        assertEquals("test", enumVS.getValue(0));
        assertEquals("test1", enumVS.getValue(1));
    }

}
