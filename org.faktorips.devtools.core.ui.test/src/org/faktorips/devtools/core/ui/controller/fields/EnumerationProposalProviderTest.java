/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.controller.fields;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumerationProposalProviderTest {
    @Mock
    private IInputFormat<String> inputFormat;

    @Mock
    private ValueDatatype valueDatatype;

    @Mock
    private EnumDatatype enumDatatype;

    @Mock
    private IValueSetOwner owner;

    private EnumerationProposalProvider enumProposalProvider;

    @Mock
    private EnumValueSet enumValueSet;

    @Mock
    private UnrestrictedValueSet unrestrictedValueSet;

    @Before
    public void setUp() throws Exception {
        enumProposalProvider = new EnumerationProposalProvider(valueDatatype, owner, inputFormat);
        when(owner.getValueSet()).thenReturn(enumValueSet);
        when(enumValueSet.isEnum()).thenReturn(true);
        when(inputFormat.format("aaaaa")).thenReturn("aaaaa");
        when(inputFormat.format("bbbbb")).thenReturn("bbbbb");
        when(inputFormat.format("ccccc")).thenReturn("ccccc");
        when(inputFormat.format(null)).thenReturn("<null>");
        when(inputFormat.format("xxxxx")).thenReturn("xxxxx");
        when(inputFormat.format("yyyyy")).thenReturn("yyyyy");
        when(inputFormat.format("zzzzz")).thenReturn("zzzzz");
        when(enumValueSet.getValuesAsList())
                .thenReturn(Arrays.asList(new String[] { "aaaaa", "bbbbb", "ccccc", null }));
        when(enumDatatype.getAllValueIds(true)).thenReturn(new String[] { "xxxxx", "yyyyy", "zzzzz", null });
    }

    @Test
    public void testGetProposals_NoEnumValueSetAndNoEnumDatatype() {
        when(owner.getValueSet()).thenReturn(unrestrictedValueSet);
        when(valueDatatype.isEnum()).thenReturn(false);

        IContentProposal[] proposals = enumProposalProvider.getProposals("", 0);

        assertEquals(0, proposals.length);
    }

    @Test
    public void testGetProposals_NoEnumValueSetAndEnumDatatype() {
        enumProposalProvider = new EnumerationProposalProvider(enumDatatype, owner, inputFormat);
        when(owner.getValueSet()).thenReturn(unrestrictedValueSet);
        when(enumDatatype.isEnum()).thenReturn(true);

        IContentProposal[] proposals = enumProposalProvider.getProposals("", 0);

        assertEquals(4, proposals.length);
        assertEquals("xxxxx", proposals[0].getLabel());
        assertEquals("yyyyy", proposals[1].getLabel());
        assertEquals("zzzzz", proposals[2].getLabel());
        assertEquals("<null>", proposals[3].getLabel());
    }

    @Test
    public void testGetProposals_NoEnumValueSetAndEnumDatatype_InvalidInput() {
        enumProposalProvider = new EnumerationProposalProvider(enumDatatype, owner, inputFormat);
        when(owner.getValueSet()).thenReturn(unrestrictedValueSet);
        when(enumDatatype.isEnum()).thenReturn(true);

        IContentProposal[] proposals = enumProposalProvider.getProposals("b", 0);

        assertEquals(0, proposals.length);
    }

    @Test
    public void testGetProposals_NoEnumValueSetAndEnumDatatype_ValidInput() {
        enumProposalProvider = new EnumerationProposalProvider(enumDatatype, owner, inputFormat);
        when(owner.getValueSet()).thenReturn(unrestrictedValueSet);
        when(enumDatatype.isEnum()).thenReturn(true);

        IContentProposal[] proposals = enumProposalProvider.getProposals("y", 0);

        assertEquals(1, proposals.length);
        assertEquals("yyyyy", proposals[0].getLabel());
    }

    @Test
    public void testGetProposals_EnumValueSetAndSomeDatatype() {
        when(owner.getValueSet()).thenReturn(enumValueSet);
        when(valueDatatype.isEnum()).thenReturn(true);

        IContentProposal[] proposals = enumProposalProvider.getProposals("", 0);

        assertEquals(4, proposals.length);
        assertEquals("aaaaa", proposals[0].getLabel());
        assertEquals("bbbbb", proposals[1].getLabel());
        assertEquals("ccccc", proposals[2].getLabel());
        assertEquals("<null>", proposals[3].getLabel());
    }

    @Test
    public void testGetProposals_EnumValueSetAndSomeDatatype_InvalidInput() {
        when(owner.getValueSet()).thenReturn(enumValueSet);
        when(valueDatatype.isEnum()).thenReturn(true);

        IContentProposal[] proposals = enumProposalProvider.getProposals("foobar", 0);

        assertEquals(0, proposals.length);
    }

    @Test
    public void testGetProposals_EnumValueSetAndSomeDatatype_ValidInput() {
        when(owner.getValueSet()).thenReturn(enumValueSet);
        when(valueDatatype.isEnum()).thenReturn(true);

        IContentProposal[] proposals = enumProposalProvider.getProposals("bb", 0);

        assertEquals(1, proposals.length);
        assertEquals("bbbbb", proposals[0].getLabel());
    }

    @Test
    public void testGetProposals_EnumDatatypeAndValueSetOwnerIsNull() {
        owner = null;
        enumProposalProvider = new EnumerationProposalProvider(enumDatatype, owner, inputFormat);
        when(enumDatatype.isEnum()).thenReturn(true);

        IContentProposal[] proposals = enumProposalProvider.getProposals("", 0);

        assertEquals(4, proposals.length);
    }

    @Test
    public void testGetProposals_SomeDatatypeAndValueSetOwnerIsNull() {
        owner = null;
        enumProposalProvider = new EnumerationProposalProvider(valueDatatype, owner, inputFormat);
        when(valueDatatype.isEnum()).thenReturn(false);

        IContentProposal[] proposals = enumProposalProvider.getProposals("", 0);

        assertEquals(0, proposals.length);
    }
}
