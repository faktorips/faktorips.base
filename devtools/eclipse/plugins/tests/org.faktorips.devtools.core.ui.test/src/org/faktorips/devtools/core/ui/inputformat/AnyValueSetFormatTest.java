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

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.productcmpt.ConfiguredValueSet;
import org.faktorips.devtools.model.internal.valueset.DelegatingValueSet;
import org.faktorips.devtools.model.internal.valueset.EnumValueSet;
import org.faktorips.devtools.model.internal.valueset.RangeValueSet;
import org.faktorips.devtools.model.internal.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.Messages;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;

public class AnyValueSetFormatTest {


    @Mock
    private IIpsObject ipsObject;

    @Mock
    private IpsUIPlugin uiPlugin;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private ConfiguredValueSet configValueSet;

    @Mock
    private IIpsModel ipsModel;

    @Mock
    private IInputFormat<String> cachedInputFormat;

    @Mock
    private ValueDatatype datatype;

    private MockitoSession mockito;

    private EnumValueSet enumValueSet;

    private RangeValueSet rangeValueSet;

    private UnrestrictedValueSet unrestrictedValueSet;

    private AnyValueSetFormat format;

    @BeforeEach
    public void setUp() throws Exception {
        mockito = createMocks(this);
        enumValueSet = new EnumValueSet(configValueSet, "ID");
        lenient().when(configValueSet.getValueSet()).thenReturn(enumValueSet);
        lenient().when(configValueSet.getIpsProject()).thenReturn(ipsProject);
        lenient().when(configValueSet.findValueDatatype(ipsProject)).thenReturn(datatype);
        lenient().when(configValueSet.getIpsModel()).thenReturn(ipsModel);
        lenient().when(uiPlugin.getInputFormat(datatype, ipsProject)).thenReturn(cachedInputFormat);
        format = new AnyValueSetFormat(configValueSet, uiPlugin);
        rangeValueSet = new RangeValueSet(configValueSet, "ID");
        unrestrictedValueSet = new UnrestrictedValueSet(configValueSet, "ID");
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
    }

    @Test
    public void testParseInternalEmptyUnrestrictedValueSet() throws Exception {
        when(configValueSet.getAllowedValueSetTypes(ipsProject)).thenReturn(
                Arrays.asList(ValueSetType.ENUM, ValueSetType.UNRESTRICTED));

        IValueSet parseInternal = format.parseInternal("");

        assertNotNull(parseInternal);
        assertTrue(parseInternal instanceof UnrestrictedValueSet);
        assertEquals(configValueSet, parseInternal.getParent());
    }

    @Test
    public void testParseInternalEmptyUnrestrictedValueSetAlreadyUnrestricted() throws Exception {
        IValueSet unrestrictedValueSet = new UnrestrictedValueSet(configValueSet, "");
        when(configValueSet.getValueSet()).thenReturn(unrestrictedValueSet);
        when(configValueSet.getAllowedValueSetTypes(ipsProject)).thenReturn(
                Arrays.asList(ValueSetType.ENUM, ValueSetType.UNRESTRICTED));

        IValueSet parseInternal = format.parseInternal("");

        assertSame(unrestrictedValueSet, parseInternal);
    }

    @Test
    public void testParseInternalEmptyReturnUnrestrictedValueSetIfValueEmpty() throws Exception {
        IValueSet parseInternal = format.parseInternal("");

        assertTrue(parseInternal instanceof UnrestrictedValueSet);
    }

    @Test
    public void testParseInternalUnrestrictedValueSet() throws Exception {
        when(configValueSet.getAllowedValueSetTypes(ipsProject)).thenReturn(
                Arrays.asList(ValueSetType.ENUM, ValueSetType.UNRESTRICTED));

        IValueSet parseInternal = format.parseInternal(Messages.ValueSetFormat_unrestricted);

        assertNotNull(parseInternal);
        assertTrue(parseInternal instanceof UnrestrictedValueSet);
        assertEquals(configValueSet, parseInternal.getParent());
    }

    @Test
    public void testParseInternal_ReturnOriginValueSetIfValueSetFormatIsNull() throws Exception {
        when(configValueSet.getAllowedValueSetTypes(ipsProject)).thenReturn(
                Arrays.asList(ValueSetType.RANGE, ValueSetType.UNRESTRICTED));

        IValueSet parseInternal = format.parseInternal("|aabc|");

        assertEquals(parseInternal, enumValueSet);
    }

    @Test
    public void testFormatInternal_delegateEnum() throws Exception {
        DelegatingValueSet delegatingValueSet = new DelegatingValueSet(enumValueSet, configValueSet);

        String formattedValue = format.formatInternal(delegatingValueSet);

        assertThat(formattedValue, is("{}"));
    }

    @Test
    public void testFormatInternal_delegateRange() throws Exception {
        DelegatingValueSet delegatingValueSet = new DelegatingValueSet(rangeValueSet, configValueSet);

        String formattedValue = format.formatInternal(delegatingValueSet);

        assertThat(formattedValue, is("[* ... *]"));
    }

    @Test
    public void testFormatInternal_delegateUnrestricted() throws Exception {
        DelegatingValueSet delegatingValueSet = new DelegatingValueSet(unrestrictedValueSet, configValueSet);

        String formattedValue = format.formatInternal(delegatingValueSet);

        assertThat(formattedValue, is(unrestrictedValueSet.toShortString()));
    }

}
