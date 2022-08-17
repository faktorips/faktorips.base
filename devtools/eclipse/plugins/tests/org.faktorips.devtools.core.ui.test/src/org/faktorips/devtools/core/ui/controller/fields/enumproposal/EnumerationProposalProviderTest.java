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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.ui.Messages;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;
import org.faktorips.devtools.model.internal.valueset.EnumValueSet;
import org.faktorips.devtools.model.internal.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumerationProposalProviderTest {

    private static final String DEFAULT_VALUE_REPRESENTATION = Messages.DefaultValueRepresentation_EditField;

    @Mock
    private IInputFormat<String> inputFormat;

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
        enumProposalProvider = new EnumerationProposalProvider(enumDatatype, owner, inputFormat);

        when(owner.getValueSet()).thenReturn(enumValueSet);
        when(enumValueSet.getValueSetOwner()).thenReturn(owner);
        when(enumValueSet.canBeUsedAsSupersetForAnotherEnumValueSet()).thenReturn(true);
        when(enumValueSet.getValuesAsList())
                .thenReturn(Arrays.asList("aaaaa", "bbbbb", "ccccc", null));

        when(inputFormat.format("aaaaa")).thenReturn("aaaaa");
        when(inputFormat.format("bbbbb")).thenReturn("bbbbb");
        when(inputFormat.format("ccccc")).thenReturn("ccccc");
        when(inputFormat.format(null)).thenReturn(DEFAULT_VALUE_REPRESENTATION);
        when(inputFormat.format("xxxxx")).thenReturn("xxxxx");
        when(inputFormat.format("yyyyy")).thenReturn("yyyyy");
        when(inputFormat.format("zzzzz")).thenReturn("zzzzz");

        when(enumDatatype.getAllValueIds(true)).thenReturn(new String[] { "xxxxx", "yyyyy", "zzzzz", null });
        when(enumDatatype.isEnum()).thenReturn(true);
    }

    @Test
    public void testGetProposals_NoEnumValueSetAndNoEnumDatatype() {
        when(owner.getValueSet()).thenReturn(unrestrictedValueSet);
        when(enumDatatype.isEnum()).thenReturn(false);

        IContentProposal[] proposals = enumProposalProvider.getProposals("", 0);

        assertEquals(0, proposals.length);
    }

    @Test
    public void testGetProposals_NoEnumValueSetAndEnumDatatype() {
        enumProposalProvider = new EnumerationProposalProvider(enumDatatype, owner, inputFormat);
        when(owner.getValueSet()).thenReturn(unrestrictedValueSet);

        IContentProposal[] proposals = enumProposalProvider.getProposals("", 0);

        assertEquals(4, proposals.length);
        assertEquals("xxxxx", proposals[0].getLabel());
        assertEquals("yyyyy", proposals[1].getLabel());
        assertEquals("zzzzz", proposals[2].getLabel());
        assertEquals(DEFAULT_VALUE_REPRESENTATION, proposals[3].getLabel());
    }

    @Test
    public void testGetProposals_NoEnumValueSetAndEnumDatatype_InvalidInput() {
        enumProposalProvider = new EnumerationProposalProvider(enumDatatype, owner, inputFormat);
        when(owner.getValueSet()).thenReturn(unrestrictedValueSet);
        when(enumDatatype.isEnum()).thenReturn(true);

        IContentProposal[] proposals = enumProposalProvider.getProposals("b", 1);

        assertEquals(0, proposals.length);
    }

    @Test
    public void testGetProposals_NoEnumValueSetAndEnumDatatype_ValidInput() {
        enumProposalProvider = new EnumerationProposalProvider(enumDatatype, owner, inputFormat);
        when(owner.getValueSet()).thenReturn(unrestrictedValueSet);
        when(enumDatatype.isEnum()).thenReturn(true);

        IContentProposal[] proposals = enumProposalProvider.getProposals("y", 1);

        assertEquals(1, proposals.length);
        assertEquals("yyyyy", proposals[0].getLabel());
    }

    @Test
    public void testGetProposals_EnumValueSetAndSomeDatatype() {
        when(owner.getValueSet()).thenReturn(enumValueSet);

        IContentProposal[] proposals = enumProposalProvider.getProposals("", 0);

        assertEquals(4, proposals.length);
        assertEquals("aaaaa", proposals[0].getLabel());
        assertEquals("bbbbb", proposals[1].getLabel());
        assertEquals("ccccc", proposals[2].getLabel());
        assertEquals(DEFAULT_VALUE_REPRESENTATION, proposals[3].getLabel());
    }

    @Test
    public void testGetProposals_EnumValueSetAndSomeDatatype_InvalidInput() {
        when(owner.getValueSet()).thenReturn(enumValueSet);

        IContentProposal[] proposals = enumProposalProvider.getProposals("foobar", 6);

        assertEquals(0, proposals.length);
    }

    @Test
    public void testGetProposals_EnumValueSetAndSomeDatatype_ValidInput() {
        when(owner.getValueSet()).thenReturn(enumValueSet);

        IContentProposal[] proposals = enumProposalProvider.getProposals("bb", 2);

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
        enumProposalProvider = new EnumerationProposalProvider(enumDatatype, owner, inputFormat);
        when(enumDatatype.isEnum()).thenReturn(false);

        IContentProposal[] proposals = enumProposalProvider.getProposals("", 0);

        assertEquals(0, proposals.length);
    }

    @Test
    public void testGetProposals_TestPosition() {
        when(owner.getValueSet()).thenReturn(enumValueSet);

        IContentProposal[] proposals = enumProposalProvider.getProposals("bar", 1);

        assertEquals(1, proposals.length);
        assertEquals("bbbbb", proposals[0].getLabel());
    }

    @Test
    public void testGetProposals_NullProposalAlreadyContained() {
        initEnumerationPPWithConfigElement();
        IContentProposal[] proposals = enumProposalProvider.getProposals("", 0);

        assertEquals(4, proposals.length);
        assertEquals("aaaaa", proposals[0].getLabel());
        assertEquals("bbbbb", proposals[1].getLabel());
        assertEquals("ccccc", proposals[2].getLabel());
        assertEquals(DEFAULT_VALUE_REPRESENTATION, proposals[3].getLabel());
    }

    @Test
    public void testGetProposals_NullProposalNotContained() {
        initEnumerationPPWithConfigElement();
        when(enumValueSet.getValuesAsList()).thenReturn(Arrays.asList("aaaaa", "bbbbb", "ccccc"));

        IContentProposal[] proposals = enumProposalProvider.getProposals("", 0);

        assertEquals(4, proposals.length);
        assertEquals("aaaaa", proposals[0].getLabel());
        assertEquals("bbbbb", proposals[1].getLabel());
        assertEquals("ccccc", proposals[2].getLabel());
        assertEquals(DEFAULT_VALUE_REPRESENTATION, proposals[3].getLabel());
    }

    @Test
    public void testGetProposals_NullProposalAlreadyContained_TextfieldContentIsNullrepresentation() {
        initEnumerationPPWithConfigElement();
        IContentProposal[] proposals = enumProposalProvider.getProposals(DEFAULT_VALUE_REPRESENTATION, 6);

        assertEquals(1, proposals.length);
        assertEquals(DEFAULT_VALUE_REPRESENTATION, proposals[0].getLabel());
    }

    @Test
    public void testGetProposals_NullProposalNotContained_TextfieldContentIsNullrepresentation() {
        initEnumerationPPWithConfigElement();
        when(enumValueSet.getValuesAsList()).thenReturn(Arrays.asList("aaaaa", "bbbbb", "ccccc"));

        IContentProposal[] proposals = enumProposalProvider.getProposals(DEFAULT_VALUE_REPRESENTATION, 6);

        assertEquals(1, proposals.length);
        assertEquals(DEFAULT_VALUE_REPRESENTATION, proposals[0].getLabel());
    }

    @Test
    public void testGetProposals_CaseSensitive() {
        initEnumerationPPWithConfigElement();
        when(inputFormat.format("foobar")).thenReturn("foobar");
        when(inputFormat.format("fooBares")).thenReturn("fooBares");
        when(enumValueSet.getValuesAsList()).thenReturn(Arrays.asList("foobar", "fooBares"));

        IContentProposal[] proposals = enumProposalProvider.getProposals("foob", 4);

        assertEquals(2, proposals.length);
        assertEquals("foobar", proposals[0].getLabel());
        assertEquals("foobar", proposals[0].getContent());
        assertEquals("fooBares", proposals[1].getLabel());
        assertEquals("fooBares", proposals[1].getContent());
    }

    private void initEnumerationPPWithConfigElement() {
        owner = mock(IConfiguredValueSet.class);
        when(owner.getValueSet()).thenReturn(enumValueSet);
        when(enumValueSet.getValueSetOwner()).thenReturn(owner);

        enumProposalProvider = new EnumerationProposalProvider(enumDatatype, owner, inputFormat);
    }

}
