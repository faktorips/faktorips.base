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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumDatatypeProposalProviderTest {

    @Mock
    private UIDatatypeFormatter uiDatatypeFormatter;

    @Mock
    private EnumDatatype enumValueDatatype;

    private EnumDatatypeProposalProvider enumProposalProvider;

    @Before
    public void setUp() throws Exception {
        enumProposalProvider = new EnumDatatypeProposalProvider(enumValueDatatype, uiDatatypeFormatter);
        when(enumValueDatatype.isEnum()).thenReturn(true);
        when(uiDatatypeFormatter.formatValue(enumValueDatatype, "aaaaa")).thenReturn("enumA aaaaa");
        when(uiDatatypeFormatter.formatValue(enumValueDatatype, "bbbbb")).thenReturn("enumB bbbbb");
        when(uiDatatypeFormatter.formatValue(enumValueDatatype, "ccccc")).thenReturn("en um C ccccc");
        when(uiDatatypeFormatter.formatValue(enumValueDatatype, "<null>")).thenReturn("<null>");
        when(enumValueDatatype.getAllValueIds(true)).thenReturn(new String[] { "aaaaa", "bbbbb", "ccccc", "<null>" });
    }

    @Test
    public void testGetProposals_NoEnumDatatype() throws Exception {
        when(enumValueDatatype.isEnum()).thenReturn(false);

        IContentProposal[] proposals = enumProposalProvider.getProposals("", 0);

        assertNotNull(proposals);
        assertEquals(0, proposals.length);
    }

    @Test
    public void testGetProposals_NoInputEntered() throws Exception {
        IContentProposal[] proposals = enumProposalProvider.getProposals("", 0);

        assertNotNull(proposals);
        assertEquals(4, proposals.length);
        assertEquals("enumA aaaaa", proposals[0].getContent());
        assertEquals("enumB bbbbb", proposals[1].getContent());
        assertEquals("en um C ccccc", proposals[2].getContent());
        assertEquals("<null>", proposals[3].getContent());
    }

    @Test
    public void testGetProposals_ValidInputEntered() throws Exception {

        IContentProposal[] proposals = enumProposalProvider.getProposals("aaa", 0);

        assertNotNull(proposals);
        assertEquals(1, proposals.length);
        assertEquals("enumA aaaaa", proposals[0].getContent());
    }

    @Test
    public void testGetProposals_FalseInputEntered() throws Exception {

        IContentProposal[] proposals = enumProposalProvider.getProposals("invalid", 0);

        assertNotNull(proposals);
        assertEquals(0, proposals.length);
    }
}
