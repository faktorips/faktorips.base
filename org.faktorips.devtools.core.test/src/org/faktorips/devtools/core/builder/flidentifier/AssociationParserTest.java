/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder.flidentifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.core.builder.flidentifier.ast.AssociationNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.util.TextRegion;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AssociationParserTest extends AbstractParserTest {

    private static final String MY_ASSOCIATION = "myAssociation";
    private static final String MY_SECOND_ASSOCIATION = "mySecondAssociation";

    private static final String ANY_ASSOCIATION = "anyAssociation";

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
        when(association.getTarget()).thenReturn("target");
        when(association.findTarget(getIpsProject())).thenReturn(targetType);
        when(policyCmptType.findAssociation(MY_SECOND_ASSOCIATION, getIpsProject())).thenReturn(association2);
        when(association2.getName()).thenReturn(MY_SECOND_ASSOCIATION);
        when(association2.getTarget()).thenReturn("target");
        when(association2.findTarget(getIpsProject())).thenReturn(targetType);

        List<IAssociation> assocList = new ArrayList<IAssociation>();
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
        assertEquals(1, proposals.size());
        proposals = associationParser.getProposals(MY_ASSOCIATION);
        assertEquals(1, proposals.size());
        assertEquals(MY_ASSOCIATION, proposals.get(0).getText());

        proposals = associationParser.getProposals("myS");
        assertEquals(1, proposals.size());
        assertEquals(MY_SECOND_ASSOCIATION, proposals.get(0).getText());
    }

    @Test
    public void testGetProposals_multipleProposals() {
        List<IdentifierProposal> proposals = associationParser.getProposals("my");
        assertEquals(2, proposals.size());
        proposals = associationParser.getProposals("m");
        assertEquals(2, proposals.size());
        proposals = associationParser.getProposals("");
        assertEquals(2, proposals.size());
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
