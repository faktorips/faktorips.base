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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.DatatypeFormatter;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ValueSetProposalProviderTest {
    @Mock
    private IConfigElement propertyValue;
    @Mock
    private IConfigElement parent;
    @Mock
    private IpsPreferences ipsPreferences;
    @Mock
    private ValueDatatype enumValueDatatype;
    @Mock
    private UnrestrictedValueSet unrestrictedValueSet;
    @Mock
    private DatatypeFormatter datatypeFormatter;

    private ValueSetProposalProvider valueSetProposalProvider;

    private EnumValueSet enumValueSet;

    @Before
    public void setUp() throws Exception {
        valueSetProposalProvider = new ValueSetProposalProvider(propertyValue, ipsPreferences);
        enumValueSet = new EnumValueSet(parent, "ID");
        when(propertyValue.getValueSet()).thenReturn(enumValueSet);
        when(enumValueSet.getValueDatatype()).thenReturn(enumValueDatatype);
        when(enumValueDatatype.isEnum()).thenReturn(false);
    }

    @Test
    public void testGetProposalsUnrestricted() throws Exception {
        when(propertyValue.getValueSet()).thenReturn(unrestrictedValueSet);

        IContentProposal[] proposals = valueSetProposalProvider.getProposals("", 0);
        assertNotNull(proposals);
        assertTrue(proposals.length == 0);
    }

    @Test
    public void testGetProposalsEmptyContent() throws Exception {
        setUpEnumValueSet();

        IContentProposal[] proposals = valueSetProposalProvider.getProposals("", 0);
        assertNotNull(proposals);
        assertTrue(proposals.length == 3);
        assertEquals("aaaaa", proposals[0].getContent());
    }

    @Test
    public void testGetProposalsOneContent() throws Exception {
        setUpEnumValueSet();

        IContentProposal[] proposals = valueSetProposalProvider.getProposals("aa", 2);
        assertNotNull(proposals);
        assertTrue(proposals.length == 1);
        assertEquals("aaaaa", proposals[0].getContent());
    }

    @Test
    public void testGetProposalsNoContent() throws Exception {
        setUpEnumValueSet();

        IContentProposal[] proposals = valueSetProposalProvider.getProposals("aaaaa", 2);
        assertNotNull(proposals);
        assertTrue(proposals.length == 0);
    }

    @Test
    public void testGetProposalsNoEnumSetValue() throws Exception {
        setUpEnumValueSet();

        IContentProposal[] proposals = valueSetProposalProvider.getProposals("dd", 2);
        assertNotNull(proposals);
        assertTrue(proposals.length == 0);
    }

    @Test
    public void testGetProposalsWithSeparator() throws Exception {
        setUpEnumValueSet();

        IContentProposal[] proposals = valueSetProposalProvider.getProposals("aaaaa | bb", 10);
        assertNotNull(proposals);
        assertTrue(String.valueOf(proposals.length), proposals.length == 1);
        assertEquals("bbbbb", proposals[0].getContent());

        proposals = valueSetProposalProvider.getProposals("aaaaa | ", 8);
        assertNotNull(proposals);
        assertTrue(String.valueOf(proposals.length), proposals.length == 2);
        assertEquals("bbbbb", proposals[0].getContent());

        proposals = valueSetProposalProvider.getProposals("aaaaa |", 7);
        assertNotNull(proposals);
        assertTrue(String.valueOf(proposals.length), proposals.length == 2);
        assertEquals("bbbbb", proposals[0].getContent());

    }

    @Test
    public void testGetProposalsWithNecessarySeparator() throws Exception {
        setUpEnumValueSet();

        IContentProposal[] proposals = valueSetProposalProvider.getProposals("aaaaa b", 10);
        assertNotNull(proposals);
        assertTrue(String.valueOf(proposals.length), proposals.length == 1);
        assertEquals("| bbbbb", proposals[0].getContent());

        proposals = valueSetProposalProvider.getProposals("aaaaa ", 10);
        assertNotNull(proposals);
        assertTrue(String.valueOf(proposals.length), proposals.length == 2);
        assertEquals("| bbbbb", proposals[0].getContent());
    }

    @Test
    public void testGetProposalsEnumDatatype() {
        enumValueSet.addValueWithoutTriggeringChangeEvent("a");
        enumValueSet.addValueWithoutTriggeringChangeEvent("b");
        enumValueSet.addValueWithoutTriggeringChangeEvent("c");
        when(ipsPreferences.getDatatypeFormatter()).thenReturn(datatypeFormatter);
        when(datatypeFormatter.formatValue(enumValueDatatype, "a")).thenReturn("aname (a)");
        when(datatypeFormatter.formatValue(enumValueDatatype, "b")).thenReturn("bname (b)");
        when(datatypeFormatter.formatValue(enumValueDatatype, "c")).thenReturn("cname (c)");

        when(enumValueDatatype.isEnum()).thenReturn(true);

        IContentProposal[] proposals = valueSetProposalProvider.getProposals("aname (a) | bn", 14);

        assertNotNull(proposals);
        assertTrue(String.valueOf(proposals.length), proposals.length == 1);
        assertEquals("bname (b)", proposals[0].getContent());

        proposals = valueSetProposalProvider.getProposals("aname (a) | ", 12);

        assertNotNull(proposals);
        assertTrue(String.valueOf(proposals.length), proposals.length == 2);
        assertEquals("bname (b)", proposals[0].getContent());
        assertEquals("cname (c)", proposals[1].getContent());
    }

    private void setUpEnumValueSet() {
        enumValueSet.addValueWithoutTriggeringChangeEvent("aaaaa");
        enumValueSet.addValueWithoutTriggeringChangeEvent("bbbbb");
        enumValueSet.addValueWithoutTriggeringChangeEvent("ccccc");
    }

}
