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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.productcmpt.ConfiguredValueSet;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.valueset.Messages;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

    @BeforeEach
    public void setUp() throws Exception {
        unrestrictedValueSetFormat = new UnrestrictedValueSetFormat(configValueSet, uiPlugin);

        when(configValueSet.getIpsProject()).thenReturn(ipsProject);
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
