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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.productcmpt.ConfiguredValueSet;
import org.faktorips.devtools.model.internal.valueset.StringLengthValueSet;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.valueset.IStringLengthValueSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class StringLengthValueSetFormatTest {

    @Mock
    private IIpsObject ipsObject;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private ConfiguredValueSet configValueSet;

    @Mock
    private IIpsModel ipsModel;

    @Mock
    private IStringLengthValueSet slvs;

    @Mock
    private IpsUIPlugin uiPlugin;

    @Mock
    private ValueDatatype datatype;

    private static final String REGULARFORMAT = NLS.bind(Messages.StringLengthValueSetFormat_Description, "10");
    private static final String UNLIMITEDFORMAT = NLS.bind(Messages.StringLengthValueSetFormat_Description,
            Messages.StringLengthValueSetFormat_Unlimited);

    private StringLengthValueSetFormat format;

    @Before
    public void setUp() throws Exception {
        format = new StringLengthValueSetFormat(configValueSet, uiPlugin);

        when(configValueSet.getIpsModel()).thenReturn(ipsModel);
    }

    @Test
    public void testIsResponsibleFor() {
        assertFalse(format.isResponsibleFor(""));
        assertTrue(format.isResponsibleFor(REGULARFORMAT));
        assertTrue(format.isResponsibleFor(REGULARFORMAT + getNullSuffix()));
        assertTrue(format.isResponsibleFor(UNLIMITEDFORMAT));
        assertTrue(format.isResponsibleFor(UNLIMITEDFORMAT + getNullSuffix()));
    }

    @Test
    public void testParseInternal() {
        IStringLengthValueSet vs = (IStringLengthValueSet)format.parseInternal(REGULARFORMAT + getNullSuffix());

        assertEquals("10", vs.getMaximumLength());
        assertEquals(true, vs.isContainsNull());
    }

    @Test
    public void testParseInternal_Unlimited() {
        IStringLengthValueSet vs = (IStringLengthValueSet)format.parseInternal(UNLIMITEDFORMAT + getNullSuffix());

        assertEquals(null, vs.getMaximumLength());
        assertEquals(true, vs.isContainsNull());
    }

    @Test
    public void testFormatInternal_IncludesNull() {
        IStringLengthValueSet vs = new StringLengthValueSet(configValueSet, "partId", "10", true);

        assertEquals(REGULARFORMAT + getNullSuffix(), format.formatInternal(vs));
    }

    @Test
    public void testFormatInternal_WithoutNull() {
        IStringLengthValueSet vs = new StringLengthValueSet(configValueSet, "partId", "10", false);

        assertEquals(REGULARFORMAT, format.formatInternal(vs));
    }

    @Test
    public void testFormatInternal_UnlimitedIncludesNull() {
        IStringLengthValueSet vs = new StringLengthValueSet(configValueSet, "partId", null, true);

        assertEquals(UNLIMITEDFORMAT + getNullSuffix(), format.formatInternal(vs));
    }

    @Test
    public void testFormatInternal_UnlimitedWithoutNull() {
        IStringLengthValueSet vs = new StringLengthValueSet(configValueSet, "partId", null, false);

        assertEquals(UNLIMITEDFORMAT, format.formatInternal(vs));
    }

    private String getNullSuffix() {
        return " " + NLS.bind(Messages.RangeValueSetFormat_includingNull,
                IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
    }
}
