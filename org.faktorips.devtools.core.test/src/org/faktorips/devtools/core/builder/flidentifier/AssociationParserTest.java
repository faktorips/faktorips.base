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

import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.core.builder.flidentifier.ast.AssociationNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
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
        associationParser = new AssociationParser(getExpression(), getIpsProject());

        when(policyCmptType.findAssociation(MY_ASSOCIATION, getIpsProject())).thenReturn(association);
        when(association.getName()).thenReturn(MY_ASSOCIATION);
        when(association.findTarget(getIpsProject())).thenReturn(targetType);
        when(policyCmptType.findAssociation(MY_SECOND_ASSOCIATION, getIpsProject())).thenReturn(association2);
        when(association2.getName()).thenReturn(MY_SECOND_ASSOCIATION);
        when(association2.findTarget(getIpsProject())).thenReturn(targetType);

        List<IAssociation> assocList = new ArrayList<IAssociation>();
        assocList.add(association);
        assocList.add(association2);
        when(policyCmptType.findAllAssociations(getIpsProject())).thenReturn(assocList);

        associationParser.setContextType(policyCmptType);
    }

    @Test
    public void testParse_noAssociationWithoutPreviousNode() throws Exception {
        IdentifierNode node = associationParser.parse(MY_ASSOCIATION, null, null);

        assertNull(node);
    }

    @Test
    public void testParse_findAssociation1To1() throws Exception {
        when(association.is1ToMany()).thenReturn(false);

        AssociationNode node = (AssociationNode)associationParser.parse(MY_ASSOCIATION, new TestNode(policyCmptType),
                null);

        assertEquals(association, node.getAssociation());
        assertEquals(targetType, node.getDatatype());
    }

    @Test
    public void testParse_findAssociation1ToMany() throws Exception {
        when(association.is1ToMany()).thenReturn(true);

        AssociationNode node = (AssociationNode)associationParser.parse(MY_ASSOCIATION, new TestNode(policyCmptType),
                null);

        assertEquals(association, node.getAssociation());
        assertEquals(new ListOfTypeDatatype(targetType), node.getDatatype());
    }

    @Test
    public void testParse_findAssociation1To1FromMany() throws Exception {
        when(association.is1ToMany()).thenReturn(false);

        AssociationNode node = (AssociationNode)associationParser.parse(MY_ASSOCIATION, new TestNode(policyCmptType,
                true), null);

        assertEquals(association, node.getAssociation());
        assertEquals(new ListOfTypeDatatype(targetType), node.getDatatype());
    }

    @Test
    public void testParse_findAssociation1ToManyFromMany() throws Exception {
        when(association.is1ToMany()).thenReturn(true);

        AssociationNode node = (AssociationNode)associationParser.parse(MY_ASSOCIATION, new TestNode(policyCmptType,
                true), null);

        assertEquals(association, node.getAssociation());
        assertEquals(new ListOfTypeDatatype(targetType), node.getDatatype());
    }

    @Test
    public void testParse_findNoAssociation() throws Exception {
        AssociationNode node = (AssociationNode)associationParser.parse(ANY_ASSOCIATION, new TestNode(
                getProductCmptType()), null);

        assertNull(node);
    }

    @Test
    public void testGetProposals_noProposals() {
        List<IdentifierNode> proposals = associationParser.getProposals("nonExistentAssociation");
        assertTrue(proposals.isEmpty());
    }

    @Test
    public void testGetProposals_oneProposal() {
        /* no pun intended ;-) */
        List<IdentifierNode> proposals = associationParser.getProposals("myAss");
        assertEquals(1, proposals.size());
        proposals = associationParser.getProposals("myAssociation");
        assertEquals(1, proposals.size());
        proposals = associationParser.getProposals("myS");
        assertEquals(1, proposals.size());
    }

    @Test
    public void testGetProposals_multipleProposals() {
        List<IdentifierNode> proposals = associationParser.getProposals("my");
        assertEquals(2, proposals.size());
        proposals = associationParser.getProposals("m");
        assertEquals(2, proposals.size());
        proposals = associationParser.getProposals("");
        assertEquals(2, proposals.size());
    }

}
