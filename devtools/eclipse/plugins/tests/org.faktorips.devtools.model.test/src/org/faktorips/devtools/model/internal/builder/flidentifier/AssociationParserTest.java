/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.builder.flidentifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.AssociationNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.util.TextRegion;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class AssociationParserTest extends AbstractParserTest {

    private static final String TARGET = "target";
    private static final String MY_ASSOCIATION = "myAssociation";
    private static final String MY_SECOND_ASSOCIATION = "mySecondAssociation";

    private static final String ANY_ASSOCIATION = "anyAssociation";

    private static final String LOCALIZED_DESCRIPTION = "Localized description";

    @Mock
    private IPolicyCmptTypeAssociation association;
    @Mock
    private IPolicyCmptTypeAssociation association2;

    @Mock
    private IPolicyCmptType policyCmptType;

    @Mock
    private IPolicyCmptType targetType;

    private AssociationParser associationParser;

    @Before
    public void mockAssociation() throws Exception {
        associationParser = new AssociationParser(getParsingContext());

        when(policyCmptType.findAssociation(MY_ASSOCIATION, getIpsProject())).thenReturn(association);
        when(association.getName()).thenReturn(MY_ASSOCIATION);
        when(association.getTarget()).thenReturn(TARGET);
        when(association.findTarget(getIpsProject())).thenReturn(targetType);
        when(association2.getName()).thenReturn(MY_SECOND_ASSOCIATION);
        when(association2.getTarget()).thenReturn(TARGET);

        List<IAssociation> assocList = new ArrayList<>();
        assocList.add(association);
        assocList.add(association2);
        when(policyCmptType.findAllAssociations(getIpsProject())).thenReturn(assocList);

        associationParser.setContextType(policyCmptType);

    }

    @Test
    public void testParse_noAssociationWithoutPreviousNode() throws Exception {
        IdentifierNode node = associationParser.parse(new TextRegion(MY_ASSOCIATION, 0, MY_ASSOCIATION.length()));

        assertNull(node);
    }

    @Test
    public void testParse_findAssociation1To1() throws Exception {
        when(association.is1ToMany()).thenReturn(false);
        getParsingContext().pushNode(new TestNode(policyCmptType));

        AssociationNode node = (AssociationNode)associationParser.parse(new TextRegion(MY_ASSOCIATION, 0,
                MY_ASSOCIATION.length()));

        assertEquals(association, node.getAssociation());
        assertEquals(targetType, node.getDatatype());
    }

    @Test
    public void testParse_findAssociation1ToMany() throws Exception {
        when(association.is1ToMany()).thenReturn(true);
        getParsingContext().pushNode(new TestNode(policyCmptType));

        AssociationNode node = (AssociationNode)associationParser.parse(new TextRegion(MY_ASSOCIATION, 0,
                MY_ASSOCIATION.length()));

        assertEquals(association, node.getAssociation());
        assertEquals(new ListOfTypeDatatype(targetType), node.getDatatype());
    }

    @Test
    public void testParse_findAssociation1To1FromMany() throws Exception {
        when(association.is1ToMany()).thenReturn(false);
        getParsingContext().pushNode(new TestNode(policyCmptType, true));

        AssociationNode node = (AssociationNode)associationParser.parse(new TextRegion(MY_ASSOCIATION, 0,
                MY_ASSOCIATION.length()));

        assertEquals(association, node.getAssociation());
        assertEquals(new ListOfTypeDatatype(targetType), node.getDatatype());
    }

    @Test
    public void testParse_findAssociation1ToManyFromMany() throws Exception {
        when(association.is1ToMany()).thenReturn(true);
        getParsingContext().pushNode(new TestNode(policyCmptType, true));

        AssociationNode node = (AssociationNode)associationParser.parse(new TextRegion(MY_ASSOCIATION, 0,
                MY_ASSOCIATION.length()));

        assertEquals(association, node.getAssociation());
        assertEquals(new ListOfTypeDatatype(targetType), node.getDatatype());
    }

    @Test
    public void testParse_findNoAssociation() throws Exception {
        getParsingContext().pushNode(new TestNode(getProductCmptType()));

        AssociationNode node = (AssociationNode)associationParser.parse(new TextRegion(ANY_ASSOCIATION, 0,
                ANY_ASSOCIATION.length()));

        assertNull(node);
    }

    @Test
    public void testGetProposals_noProposals() {
        List<IdentifierProposal> proposals = associationParser.getProposals("nonExistentAssociation");

        assertTrue(proposals.isEmpty());
    }

    @Test
    public void testGetProposals_oneProposal() {
        /* no pun intended ;-) */
        List<IdentifierProposal> proposals = associationParser.getProposals("myAss");
        assertEquals(3, proposals.size());
        proposals = associationParser.getProposals(MY_ASSOCIATION);
        assertEquals(3, proposals.size());
        assertEquals(MY_ASSOCIATION, proposals.get(0).getText());

        proposals = associationParser.getProposals("myS");
        assertEquals(3, proposals.size());
        assertEquals(MY_SECOND_ASSOCIATION, proposals.get(0).getText());
        assertEquals(MY_SECOND_ASSOCIATION + "[0]", proposals.get(1).getText());
        assertEquals(MY_SECOND_ASSOCIATION + "[\"", proposals.get(2).getText());
    }

    @Test
    public void testGetDisplayText() {
        when(association.is1ToMany()).thenReturn(false);
        when(association.is1ToManyIgnoringQualifier()).thenReturn(true);
        when(association2.is1ToMany()).thenReturn(true);
        when(association2.is1ToManyIgnoringQualifier()).thenReturn(false);

        List<IdentifierProposal> proposals = associationParser.getProposals("");

        assertEquals(6, proposals.size());
        assertEquals(getDisplayText(MY_ASSOCIATION, IpsStringUtils.EMPTY, false), proposals.get(0).getLabel());
        assertEquals(getDisplayText(MY_ASSOCIATION, AssociationParser.INDEX_PROPOSAL, false), proposals.get(1)
                .getLabel());
        assertEquals(getDisplayText(MY_ASSOCIATION, AssociationParser.QUALIFIER_PROPOSAL_LABEL, true), proposals.get(2)
                .getLabel());
        assertEquals(getDisplayText(MY_SECOND_ASSOCIATION, IpsStringUtils.EMPTY, true), proposals.get(3).getLabel());
        assertEquals(getDisplayText(MY_SECOND_ASSOCIATION, AssociationParser.INDEX_PROPOSAL, false), proposals.get(4)
                .getLabel());
        assertEquals(getDisplayText(MY_SECOND_ASSOCIATION, AssociationParser.QUALIFIER_PROPOSAL_LABEL, false),
                proposals.get(5).getLabel());
    }

    private String getDisplayText(String associationName, String suffix, boolean listTarget) {
        String result = associationName + suffix + AssociationParser.ASSOCIATION_TARGET_SEPERATOR;
        if (listTarget) {
            result += NLS.bind(Messages.AssociationParser_ListDatatypeDescriptionPrefix, TARGET);
        } else {
            result += TARGET;
        }
        return result;
    }

    @Test
    public void testGetDescription() {
        when(association.is1ToMany()).thenReturn(false);
        when(association.is1ToManyIgnoringQualifier()).thenReturn(true);
        when(getMultiLanguageSupport().getLocalizedDescription(association)).thenReturn(LOCALIZED_DESCRIPTION);
        when(association2.is1ToMany()).thenReturn(true);
        when(association2.is1ToManyIgnoringQualifier()).thenReturn(false);
        when(getMultiLanguageSupport().getLocalizedDescription(association2)).thenReturn(LOCALIZED_DESCRIPTION);

        List<IdentifierProposal> proposals = associationParser.getProposals("");

        assertEquals(6, proposals.size());
        assertEquals(getDescriptionText(MY_ASSOCIATION, IpsStringUtils.EMPTY, false),
                proposals.get(0).getDescription());
        assertEquals(getDescriptionText(MY_ASSOCIATION, AssociationParser.INDEX_PROPOSAL, false), proposals.get(1)
                .getDescription());
        assertEquals(getDescriptionText(MY_ASSOCIATION, AssociationParser.QUALIFIER_PROPOSAL_LABEL, true), proposals
                .get(2).getDescription());
        assertEquals(getDescriptionText(MY_SECOND_ASSOCIATION, IpsStringUtils.EMPTY, true), proposals.get(3)
                .getDescription());
        assertEquals(getDescriptionText(MY_SECOND_ASSOCIATION, AssociationParser.INDEX_PROPOSAL, false),
                proposals.get(4).getDescription());
        assertEquals(getDescriptionText(MY_SECOND_ASSOCIATION, AssociationParser.QUALIFIER_PROPOSAL_LABEL, false),
                proposals.get(5).getDescription());
    }

    private String getDescriptionText(String associationName, String suffix, boolean listTarget) {
        String result = getDisplayText(associationName, suffix, listTarget);
        result += "\n\n";
        if (suffix.equals(IpsStringUtils.EMPTY)) {
            result += LOCALIZED_DESCRIPTION;
        } else if (suffix.equals(AssociationParser.INDEX_PROPOSAL)) {
            result += Messages.QualifierAndIndexParser_descriptionIndex + "\n\n" + LOCALIZED_DESCRIPTION;
        } else if (suffix.equals(AssociationParser.QUALIFIER_PROPOSAL_LABEL)) {
            result += Messages.QualifierAndIndexParser_descriptionQualifierUndefined + "\n\n" + LOCALIZED_DESCRIPTION;
        }
        return result;
    }

    @Test
    public void testGetProposals_multipleProposals() {
        List<IdentifierProposal> proposals = associationParser.getProposals("my");
        assertEquals(6, proposals.size());
        proposals = associationParser.getProposals("m");
        assertEquals(6, proposals.size());
        proposals = associationParser.getProposals("");
        assertEquals(6, proposals.size());
    }

    @Test
    public void testGetProposals_wrongContextType() {
        associationParser.setContextType(Datatype.MONEY);

        List<IdentifierProposal> proposals = associationParser.getProposals("my");
        assertTrue(proposals.isEmpty());
        proposals = associationParser.getProposals("m");
        assertTrue(proposals.isEmpty());
        proposals = associationParser.getProposals("");
        assertTrue(proposals.isEmpty());
    }

}
