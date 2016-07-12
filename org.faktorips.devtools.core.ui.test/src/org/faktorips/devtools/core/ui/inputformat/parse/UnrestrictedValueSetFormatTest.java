/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.inputformat.parse;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.productcmpt.ConfiguredValueSet;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IUnrestrictedValueSet;
import org.faktorips.devtools.core.model.valueset.Messages;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.inputformat.DefaultInputFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UnrestrictedValueSetFormatTest {

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

    private UnrestrictedValueSetFormat unrestrictedValueSetFormat;

    private IUnrestrictedValueSet valueSet;

    @Before
    public void setUp() throws Exception {
        unrestrictedValueSetFormat = new UnrestrictedValueSetFormat(configValueSet, uiPlugin);
        valueSet = new UnrestrictedValueSet(configValueSet, "2", true);

        when(uiPlugin.getInputFormat(datatype, ipsProject)).thenReturn(new DefaultInputFormat(null));
        when(configValueSet.findValueDatatype(ipsProject)).thenReturn(datatype);
        when(configValueSet.getIpsProject()).thenReturn(ipsProject);
        when(configValueSet.getIpsModel()).thenReturn(ipsModel);
        when(configValueSet.getIpsObject()).thenReturn(ipsObject);
        when(configValueSet.getValueSet()).thenReturn(valueSet);
        when(configValueSet.getAllowedValueSetTypes(ipsProject)).thenReturn(Arrays.asList(ValueSetType.UNRESTRICTED));
    }

    @Test
    public void testIsResponsibleFor_ReturnTrue_IfStringToBeParsed_unrestricted() {
        assertTrue(unrestrictedValueSetFormat.isResponsibleFor(Messages.ValueSetFormat_unrestricted));
    }

    @Test
    public void testIsResponsibleFor_ReturnTrue_IfStringToBeParsed_unrestrictedWithoutNull() {
        assertTrue(unrestrictedValueSetFormat.isResponsibleFor(Messages.ValueSet_unrestrictedWithoutNull));
    }

    @Test
    public void testIsResponsibleFor_ReturnTrueIfOnlyUnrestrictedValueSetTypeIsAllowed() {
        assertTrue(unrestrictedValueSetFormat.isResponsibleFor("abc"));
    }
}
