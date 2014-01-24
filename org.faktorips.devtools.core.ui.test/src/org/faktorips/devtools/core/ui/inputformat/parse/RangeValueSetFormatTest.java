/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.inputformat.parse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.productcmpt.ConfigElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.inputformat.DefaultInputFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RangeValueSetFormatTest {

    @Mock
    private IIpsObject ipsObject;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private ConfigElement configElement;

    @Mock
    private IIpsModel ipsModel;

    @Mock
    private IRangeValueSet range;

    private RangeValueSetFormat rangeVSFormat;

    @Mock
    private IpsUIPlugin uiPlugin;

    @Mock
    private ValueDatatype datatype;

    @Before
    public void setUp() throws Exception {
        rangeVSFormat = new RangeValueSetFormat(configElement, uiPlugin);

        when(uiPlugin.getInputFormat(Mockito.any(ValueDatatype.class), Mockito.any(IIpsProject.class))).thenReturn(
                new DefaultInputFormat());
        when(configElement.findValueDatatype(ipsProject)).thenReturn(datatype);
        when(configElement.getValueSet()).thenReturn(range);
        when(configElement.getIpsModel()).thenReturn(ipsModel);
        when(configElement.getIpsObject()).thenReturn(ipsObject);
    }

    @Test
    public void testParseInternalRange() {
        IValueSet parseInternal = rangeVSFormat.parseInternal("[10 .. 100/ 2]");
        IRangeValueSet result = (IRangeValueSet)parseInternal;

        assertNotNull(parseInternal);
        assertEquals(result.getLowerBound(), "10");
        assertEquals(result.getUpperBound(), "100");
        assertEquals(result.getStep(), "2");
    }

    @Test
    public void testParseRangeValueStepNull() {
        IValueSet parseInternal = rangeVSFormat.parseInternal("[10 .. 2/]");
        IRangeValueSet result = (IRangeValueSet)parseInternal;

        assertNotNull(parseInternal);
        assertEquals(result.getLowerBound(), "10");
        assertEquals(result.getUpperBound(), "2");
        assertEquals(result.getStep(), "");
    }

    @Test
    public void testParseRangeValueEmptyRange() {
        IValueSet parseInternal = rangeVSFormat.parseInternal("[]");
        IRangeValueSet result = (IRangeValueSet)parseInternal;

        assertEquals(result.getLowerBound(), null);
        assertEquals(result.getUpperBound(), null);
        assertEquals(result.getStep(), null);
    }

    @Test
    public void testParseRangeValueUpperBoundEmpty() {
        IValueSet parseInternal = rangeVSFormat.parseInternal("[10 .. /2]");
        IRangeValueSet result = (IRangeValueSet)parseInternal;

        assertNotNull(parseInternal);
        assertEquals(result.getLowerBound(), "10");
        assertEquals(result.getUpperBound(), "");
        assertEquals(result.getStep(), "2");
    }

    @Test
    public void testParseRangeValueTooManyPonits() {
        IValueSet parseInternal = rangeVSFormat.parseInternal("[10 .. 3 ..2]");
        IRangeValueSet result = (IRangeValueSet)parseInternal;

        assertNotNull(parseInternal);
        assertEquals(result.getLowerBound(), "10");
        assertEquals(result.getUpperBound(), "3 ..2");
        assertEquals(result.getStep(), null);
    }
}
