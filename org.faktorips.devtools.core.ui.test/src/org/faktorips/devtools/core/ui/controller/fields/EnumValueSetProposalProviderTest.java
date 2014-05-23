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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumValueSetProposalProviderTest {

    @Mock
    private UIDatatypeFormatter uiDatatypeFormatter;

    @Mock
    private ValueDatatype valueDatatype;

    @Mock
    private IValueSetOwner owner;

    private EnumValueSetProposalProvider enumProposalProvider;

    @Mock
    private EnumValueSet enumValueSet;

    @Before
    public void setUp() throws Exception {
        enumProposalProvider = new EnumValueSetProposalProvider(owner, valueDatatype, uiDatatypeFormatter);
        when(owner.getValueSet()).thenReturn(enumValueSet);
        when(enumValueSet.isEnum()).thenReturn(true);
        when(uiDatatypeFormatter.formatValue(valueDatatype, "aaaaa")).thenReturn("Integer aaaaa");
        when(uiDatatypeFormatter.formatValue(valueDatatype, "bbbbb")).thenReturn("Integer bbbbb");
        when(uiDatatypeFormatter.formatValue(valueDatatype, "ccccc")).thenReturn("Integer ccccc");
        when(uiDatatypeFormatter.formatValue(valueDatatype, "<null>")).thenReturn("<null>");
        when(enumValueSet.getValuesAsList()).thenReturn(
                Arrays.asList(new String[] { "aaaaa", "bbbbb", "ccccc", "<null>" }));
    }

    @Test
    public void testGetProposals_NoEnumValueSet() {
        when(enumValueSet.isEnum()).thenReturn(true);

        IContentProposal[] proposals = enumProposalProvider.getProposals("contents", 0);

        assertArrayEquals(proposals, new IContentProposal[0]);

    }

    @Test
    public void testGetProposals_EmptyInput() {

        IContentProposal[] proposals = enumProposalProvider.getProposals("", 0);

        assertEquals(4, proposals.length);
        assertEquals("Integer aaaaa", proposals[0].getContent());
        assertEquals("Integer bbbbb", proposals[1].getContent());
        assertEquals("Integer ccccc", proposals[2].getContent());
        assertEquals("<null>", proposals[3].getContent());
    }

    @Test
    public void testGetProposals_InvalidInput() {

        IContentProposal[] proposals = enumProposalProvider.getProposals("foobar", 0);

        assertEquals(0, proposals.length);
    }

    @Test
    public void testGetProposals_ValidInput() {

        IContentProposal[] proposals = enumProposalProvider.getProposals("Integer bb", 0);

        assertEquals(1, proposals.length);
        assertEquals("Integer bbbbb", proposals[0].getContent());
    }

    @Test
    public void testGetProposals_ValidInputIgnoreCaseSensitive() {

        IContentProposal[] proposals = enumProposalProvider.getProposals("iNTEGER bb", 0);

        assertEquals(1, proposals.length);
        assertEquals("Integer bbbbb", proposals[0].getContent());
    }
}
