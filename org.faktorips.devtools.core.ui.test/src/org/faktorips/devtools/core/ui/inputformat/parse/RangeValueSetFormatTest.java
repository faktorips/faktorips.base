/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.productcmpt.ConfiguredValueSet;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
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
    private ConfiguredValueSet configValueSet;

    @Mock
    private IIpsModel ipsModel;

    @Mock
    private IRangeValueSet range;

    private RangeValueSetFormat rangeVSFormat;

    @Mock
    private IpsUIPlugin uiPlugin;

    @Mock
    private ValueDatatype datatype;

    private final static String NULL_PRESENTATION = NLS.bind(Messages.RangeValueSetFormat_includingNull, IpsPlugin
            .getDefault().getIpsPreferences().getNullPresentation());

    @Before
    public void setUp() throws Exception {
        rangeVSFormat = new RangeValueSetFormat(configValueSet, uiPlugin);

        when(uiPlugin.getInputFormat(Mockito.any(ValueDatatype.class), Mockito.any(IIpsProject.class))).thenReturn(
                new DefaultInputFormat(null));
        when(configValueSet.findValueDatatype(ipsProject)).thenReturn(datatype);
        when(configValueSet.getValueSet()).thenReturn(range);
        when(configValueSet.getIpsModel()).thenReturn(ipsModel);
        when(configValueSet.getIpsObject()).thenReturn(ipsObject);
    }

    @Test
    public void testParseInternal_Range() {
        IValueSet parseInternal = rangeVSFormat.parseInternal("[10 .. 100/ 2]");
        IRangeValueSet result = (IRangeValueSet)parseInternal;

        assertNotNull(parseInternal);
        assertEquals(result.getLowerBound(), "10");
        assertEquals(result.getUpperBound(), "100");
        assertEquals(result.getStep(), "2");
    }

    @Test
    public void testParseInternal_RangeValueStepNull() {
        IValueSet parseInternal = rangeVSFormat.parseInternal("[10 .. 2/]" + NULL_PRESENTATION);
        IRangeValueSet result = (IRangeValueSet)parseInternal;

        assertNotNull(parseInternal);
        assertEquals(result.getLowerBound(), "10");
        assertEquals(result.getUpperBound(), "2");
        assertEquals(result.getStep(), null);
        assertTrue(result.isContainsNull());
    }

    @Test
    public void testParseInternal_RangeValueEmptyRange() {
        IValueSet parseInternal = rangeVSFormat.parseInternal("[]" + NULL_PRESENTATION);
        IRangeValueSet result = (IRangeValueSet)parseInternal;

        assertEquals(result.getLowerBound(), null);
        assertEquals(result.getUpperBound(), null);
        assertEquals(result.getStep(), null);
        assertFalse(result.isContainsNull());
    }

    @Test
    public void testParseInternal_RangeValueUpperBoundEmpty() {
        IValueSet parseInternal = rangeVSFormat.parseInternal("[10 .. /2]" + NULL_PRESENTATION);
        IRangeValueSet result = (IRangeValueSet)parseInternal;

        assertNotNull(parseInternal);
        assertEquals(result.getLowerBound(), "10");
        assertEquals(result.getUpperBound(), null);
        assertEquals(result.getStep(), "2");
        assertTrue(result.isContainsNull());
    }

    @Test
    public void testParseInternal_RangeValueTooManyPonits() {
        IValueSet parseInternal = rangeVSFormat.parseInternal("[10 .. 3 ..2]" + NULL_PRESENTATION);
        IRangeValueSet result = (IRangeValueSet)parseInternal;

        assertNotNull(parseInternal);
        assertEquals(result.getLowerBound(), "10");
        assertEquals(result.getUpperBound(), "3 ..2");
        assertEquals(result.getStep(), null);
        assertTrue(result.isContainsNull());
    }

    @Test
    public void testParseInternal_RangeIncludesNull() {
        IValueSet parseInternal = rangeVSFormat.parseInternal("[10 .. 100/ 2]" + NULL_PRESENTATION);
        IRangeValueSet result = (IRangeValueSet)parseInternal;

        assertNotNull(parseInternal);
        assertEquals(result.getLowerBound(), "10");
        assertEquals(result.getUpperBound(), "100");
        assertEquals(result.getStep(), "2");
        assertTrue(result.isContainsNull());
    }

    @Test
    public void testParseInternal_RangeIncludesNullWithoutBrackets() {
        IValueSet parseInternal = rangeVSFormat.parseInternal("10 .. 100/ 2" + ' ' + NULL_PRESENTATION);
        IRangeValueSet result = (IRangeValueSet)parseInternal;

        assertNotNull(parseInternal);
        assertEquals(result.getLowerBound(), "10");
        assertEquals(result.getUpperBound(), "100");
        assertEquals(result.getStep(), "2");
        assertTrue(result.isContainsNull());
    }

    @Test
    public void testParseInternal_RangeIncludesNullWithBlank() {
        IValueSet parseInternal = rangeVSFormat.parseInternal("[10 .. 100/ 2]" + ' ' + NULL_PRESENTATION);
        IRangeValueSet result = (IRangeValueSet)parseInternal;

        assertNotNull(parseInternal);
        assertEquals(result.getLowerBound(), "10");
        assertEquals(result.getUpperBound(), "100");
        assertEquals(result.getStep(), "2");
        assertTrue(result.isContainsNull());
    }

    @Test
    public void testParseInternal_Range_NoCorrectContainsNullPresentation() {
        IValueSet parseInternal = rangeVSFormat.parseInternal("[10 .. 100/ 2]" + ' ' + "WrongInclNull");
        IRangeValueSet result = (IRangeValueSet)parseInternal;

        assertNotNull(parseInternal);
        assertEquals(result.getLowerBound(), "10");
        assertEquals(result.getUpperBound(), "100");
        assertEquals(result.getStep(), "2] WrongInclNull");
        assertFalse(result.isContainsNull());
    }

    @Test
    public void testParseInternal_RangeNotIncludesNullWithBlank() {
        IValueSet parseInternal = rangeVSFormat.parseInternal("[10 .. 100/ 2]" + ' ');
        IRangeValueSet result = (IRangeValueSet)parseInternal;

        assertNotNull(parseInternal);
        assertEquals(result.getLowerBound(), "10");
        assertEquals(result.getUpperBound(), "100");
        assertEquals(result.getStep(), "2");
        assertFalse(result.isContainsNull());
    }

    @Test
    public void testParseInternal_RangeExcludesNull() {
        IValueSet parseInternal = rangeVSFormat.parseInternal("[10 .. 100/ 2]");
        IRangeValueSet result = (IRangeValueSet)parseInternal;

        assertNotNull(parseInternal);
        assertFalse(result.isContainsNull());
    }

    @Test
    public void testFormatInternal_RangeIncludesNull() {
        IValueSet valueSet = new RangeValueSet(configValueSet, "partId", "1", "10", "1");
        valueSet.setContainsNull(true);
        when(configValueSet.getValueSet()).thenReturn(valueSet);

        assertEquals("[1 ... 10 / 1] " + NULL_PRESENTATION, rangeVSFormat.formatInternal(valueSet));
    }

    @Test
    public void testFormatInternal_RangeExcludesNull() {
        IValueSet valueSet = new RangeValueSet(configValueSet, "partId", "1", "10", "1");
        valueSet.setContainsNull(false);
        when(configValueSet.getValueSet()).thenReturn(valueSet);

        assertEquals("[1 ... 10 / 1]", rangeVSFormat.formatInternal(valueSet));
    }
}
