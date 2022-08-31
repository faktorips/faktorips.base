/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields.enumproposal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;
import org.faktorips.devtools.model.internal.productcmpt.ConfiguredValueSet;
import org.faktorips.devtools.model.internal.valueset.EnumValueSet;
import org.faktorips.devtools.model.internal.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConfigElementProposalProviderTest {
    @Mock
    private ConfiguredValueSet propertyValue;

    @Mock
    private IInputFormat<String> inputFormat;

    @Mock
    private EnumDatatype enumValueDatatype;

    @Mock
    private UnrestrictedValueSet unrestrictedValueSet;

    @Mock
    private IIpsObject ipsObject;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IPolicyCmptTypeAttribute policyCmptTypeAttribute;

    private ConfigElementProposalProvider valueSetProposalProvider;

    private EnumValueSet enumValueSet;

    @Before
    public void setUp() throws Exception {
        enumValueSet = new EnumValueSet(propertyValue, "ID");
        when(enumValueSet.findValueDatatype(ipsProject)).thenReturn(enumValueDatatype);
        when(propertyValue.getIpsProject()).thenReturn(ipsProject);
        when(propertyValue.getIpsObject()).thenReturn(ipsObject);
        when(propertyValue.findPcTypeAttribute(ipsProject)).thenReturn(policyCmptTypeAttribute);
        when(propertyValue.findValueDatatype(ipsProject)).thenReturn(enumValueDatatype);
        when(propertyValue.getValueSet()).thenReturn(new UnrestrictedValueSet(propertyValue, "123"));
        when(propertyValue.getAllowedValueSetTypes(ipsProject)).thenReturn(
                Arrays.asList(ValueSetType.UNRESTRICTED, ValueSetType.ENUM));
        when(policyCmptTypeAttribute.getValueSet()).thenReturn(enumValueSet);
        doReturn("enumA aaaaa").when(inputFormat).format("aaaaa");
        doReturn("enumB bbbbb").when(inputFormat).format("bbbbb");
        doReturn("en um C ccccc").when(inputFormat).format("ccccc");
        valueSetProposalProvider = new ConfigElementProposalProvider(propertyValue, enumValueDatatype, inputFormat);
    }

    @Test
    public void testGetProposalsUnrestricted() throws Exception {
        IContentProposal[] proposals = valueSetProposalProvider.getProposals("", 0);

        assertNotNull(proposals);
        assertEquals(0, proposals.length);
    }

    @Test
    public void testGetProposalsEmptyContent() throws Exception {
        setUpEnumValueSet();

        IContentProposal[] proposals = valueSetProposalProvider.getProposals("", 0);

        assertNotNull(proposals);
        assertEquals(3, proposals.length);
        assertEquals("enumA aaaaa", proposals[0].getContent());
    }

    @Test
    public void testGetProposalsOneContent() throws Exception {
        setUpEnumValueSet();

        IContentProposal[] proposals = valueSetProposalProvider.getProposals("enumA", 5);

        assertNotNull(proposals);
        assertEquals(1, proposals.length);
        assertEquals("enumA aaaaa", proposals[0].getContent());
    }

    @Test
    public void testGetProposalsOneContent_withWhitespace() throws Exception {
        setUpEnumValueSet();

        IContentProposal[] proposals = valueSetProposalProvider.getProposals("en um ", 6);

        assertNotNull(proposals);
        assertEquals(1, proposals.length);
        assertEquals("en um C ccccc", proposals[0].getContent());
    }

    @Test
    public void testGetProposalsOneContent_withCamelCase() throws Exception {
        setUpEnumValueSet();

        IContentProposal[] proposals = valueSetProposalProvider.getProposals("EB", 2);

        assertNotNull(proposals);
        assertEquals(1, proposals.length);
        assertEquals("enumB bbbbb", proposals[0].getContent());
    }

    @Test
    public void testGetProposalsNoContent() throws Exception {
        setUpEnumValueSet();

        IContentProposal[] proposals = valueSetProposalProvider.getProposals("aaaaa", 2);

        assertNotNull(proposals);
        assertEquals(0, proposals.length);
    }

    @Test
    public void testGetProposalsNoEnumSetValue() throws Exception {
        setUpEnumValueSet();

        IContentProposal[] proposals = valueSetProposalProvider.getProposals("dd", 2);

        assertNotNull(proposals);
        assertEquals(0, proposals.length);
    }

    @Test
    public void testGetProposalsWithSeparator() throws Exception {
        setUpEnumValueSet();

        IContentProposal[] proposals = valueSetProposalProvider.getProposals("enumA aaaa | enumB", 18);

        assertNotNull(proposals);
        assertEquals(1, proposals.length);
        assertEquals("enumB bbbbb", proposals[0].getContent());
    }

    @Test
    public void testGetProposals_alreadyContainingValue() throws Exception {
        setUpEnumValueSet();
        when(propertyValue.getValueSet()).thenReturn(new EnumValueSet(propertyValue, Arrays.asList("aaaaa"), "123"));

        IContentProposal[] proposals = valueSetProposalProvider.getProposals("enumA aaaaa | ", 14);

        assertNotNull(proposals);
        assertEquals(2, proposals.length);
        assertEquals("enumB bbbbb", proposals[0].getContent());
    }

    @Test
    public void testGetProposals_alreadyContaining_noWhitespace() throws Exception {
        setUpEnumValueSet();
        when(propertyValue.getValueSet()).thenReturn(new EnumValueSet(propertyValue, Arrays.asList("aaaaa"), "123"));

        IContentProposal[] proposals = valueSetProposalProvider.getProposals("enumA aaaaa |", 13);

        assertNotNull(proposals);
        assertEquals(2, proposals.length);
        assertEquals("enumB bbbbb", proposals[0].getContent());
    }

    @Test
    public void testGetProposals_alreadyContaining_editMiddleElement() throws Exception {
        setUpEnumValueSet();
        when(propertyValue.getValueSet()).thenReturn(new EnumValueSet(propertyValue, Arrays.asList("aaaaa"), "123"));

        IContentProposal[] proposals = valueSetProposalProvider.getProposals("enumA aaaaa | enumB | asfasdf", 19);

        assertNotNull(proposals);
        assertEquals(1, proposals.length);
        assertEquals("enumB bbbbb", proposals[0].getContent());
    }

    @Test
    public void testGetProposals_invalidPreviousValue() throws Exception {
        setUpEnumValueSet();
        when(propertyValue.getValueSet()).thenReturn(new EnumValueSet(propertyValue, "123"));

        IContentProposal[] proposals = valueSetProposalProvider.getProposals("xasds |", 8);

        assertNotNull(proposals);
        assertEquals(3, proposals.length);
        assertEquals("enumA aaaaa", proposals[0].getContent());
    }

    @Test
    public void testGetProposals_CaseInSensitive() {
        enumValueSet.addValue("foobar");
        enumValueSet.addValue("fooBares");
        when(inputFormat.format("foobar")).thenReturn("foobar");
        when(inputFormat.format("fooBares")).thenReturn("fooBares");

        IContentProposal[] proposals = valueSetProposalProvider.getProposals("foob", 4);

        assertNotNull(proposals);
        assertEquals(2, proposals.length);
        assertEquals("foobar", proposals[0].getLabel());
        assertEquals("foobar", proposals[0].getContent());
        assertEquals("fooBares", proposals[1].getLabel());
        assertEquals("fooBares", proposals[1].getContent());
    }

    private void setUpEnumValueSet() {
        enumValueSet.addValue("aaaaa");
        enumValueSet.addValue("bbbbb");
        enumValueSet.addValue("ccccc");
    }

}
