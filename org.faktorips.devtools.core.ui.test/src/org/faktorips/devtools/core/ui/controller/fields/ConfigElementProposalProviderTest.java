/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.productcmpt.ConfigElement;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;
import org.faktorips.devtools.core.ui.controller.fields.ConfigElementProposalProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConfigElementProposalProviderTest {
    @Mock
    private ConfigElement propertyValue;

    @Mock
    private UIDatatypeFormatter uiDatatypeFormatter;

    @Mock
    private ValueDatatype enumValueDatatype;

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
        valueSetProposalProvider = new ConfigElementProposalProvider(propertyValue, uiDatatypeFormatter);
        enumValueSet = new EnumValueSet(propertyValue, "ID");
        when(enumValueSet.getValueDatatype()).thenReturn(enumValueDatatype);
        when(enumValueDatatype.isEnum()).thenReturn(true);
        when(enumValueDatatype.isParsable("aaaaa")).thenReturn(true);
        when(enumValueDatatype.isParsable("bbbbb")).thenReturn(true);
        when(enumValueDatatype.isParsable("ccccc")).thenReturn(true);
        when(enumValueDatatype.areValuesEqual("aaaaa", "aaaaa")).thenReturn(true);
        when(enumValueDatatype.areValuesEqual("bbbbb", "bbbbb")).thenReturn(true);
        when(enumValueDatatype.areValuesEqual("ccccc", "ccccc")).thenReturn(true);
        when(propertyValue.getIpsProject()).thenReturn(ipsProject);
        when(propertyValue.getIpsObject()).thenReturn(ipsObject);
        when(propertyValue.findPcTypeAttribute(ipsProject)).thenReturn(policyCmptTypeAttribute);
        when(propertyValue.findValueDatatype(ipsProject)).thenReturn(enumValueDatatype);
        when(propertyValue.getValueSet()).thenReturn(new UnrestrictedValueSet(propertyValue, "123"));
        when(propertyValue.getAllowedValueSetTypes(ipsProject)).thenReturn(
                Arrays.asList(ValueSetType.UNRESTRICTED, ValueSetType.ENUM));
        when(policyCmptTypeAttribute.getValueSet()).thenReturn(enumValueSet);
        when(uiDatatypeFormatter.formatValue(enumValueDatatype, "aaaaa")).thenReturn("enumA aaaaa");
        when(uiDatatypeFormatter.formatValue(enumValueDatatype, "bbbbb")).thenReturn("enumB bbbbb");
        when(uiDatatypeFormatter.formatValue(enumValueDatatype, "ccccc")).thenReturn("en um C ccccc");
    }

    @Test
    public void testGetProposalsUnrestricted() throws Exception {
        when(propertyValue.getValueSet()).thenReturn(unrestrictedValueSet);

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
        assertEquals(" aaaaa", proposals[0].getContent());
    }

    @Test
    public void testGetProposalsOneContent_withWhitespace() throws Exception {
        setUpEnumValueSet();

        IContentProposal[] proposals = valueSetProposalProvider.getProposals("en um ", 6);

        assertNotNull(proposals);
        assertEquals(1, proposals.length);
        assertEquals("C ccccc", proposals[0].getContent());
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
        assertEquals(" bbbbb", proposals[0].getContent());
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
        assertEquals(" bbbbb", proposals[0].getContent());
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

    private void setUpEnumValueSet() {
        enumValueSet.addValue("aaaaa");
        enumValueSet.addValue("bbbbb");
        enumValueSet.addValue("ccccc");
    }

}
