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
import org.faktorips.devtools.core.internal.model.productcmpt.ConfigElement;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumDatatypeProposalProviderTest {
    @Mock
    private ConfigElement propertyValue;

    @Mock
    private UIDatatypeFormatter uiDatatypeFormatter;

    @Mock
    private EnumDatatype enumValueDatatype;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IPolicyCmptTypeAttribute policyCmptTypeAttribute;

    private EnumDatatypeProposalProvider enumProposalProvider;

    private EnumValueSet enumValueSet;

    @Before
    public void setUp() throws Exception {
        enumProposalProvider = new EnumDatatypeProposalProvider(propertyValue, uiDatatypeFormatter);
        enumValueSet = new EnumValueSet(propertyValue, "ID");
        when(enumValueSet.getValueDatatype()).thenReturn(enumValueDatatype);
        when(enumValueDatatype.isEnum()).thenReturn(true);
        when(propertyValue.getIpsProject()).thenReturn(ipsProject);
        when(propertyValue.findPcTypeAttribute(ipsProject)).thenReturn(policyCmptTypeAttribute);
        when(propertyValue.findValueDatatype(ipsProject)).thenReturn(enumValueDatatype);
        when(policyCmptTypeAttribute.getValueSet()).thenReturn(enumValueSet);
        when(uiDatatypeFormatter.formatValue(enumValueDatatype, "aaaaa")).thenReturn("enumA aaaaa");
        when(uiDatatypeFormatter.formatValue(enumValueDatatype, "bbbbb")).thenReturn("enumB bbbbb");
        when(uiDatatypeFormatter.formatValue(enumValueDatatype, "ccccc")).thenReturn("en um C ccccc");
        when(enumValueDatatype.getAllValueIds(false)).thenReturn(new String[] { "aaaaa", "bbbbb", "ccccc" });
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
        assertEquals(3, proposals.length);
        assertEquals("enumA aaaaa", proposals[0].getContent());
        assertEquals("enumB bbbbb", proposals[1].getContent());
        assertEquals("en um C ccccc", proposals[2].getContent());
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
