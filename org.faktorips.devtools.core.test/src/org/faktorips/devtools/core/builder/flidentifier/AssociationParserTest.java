/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder.flidentifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.core.builder.flidentifier.ast.AssociationNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AssociationParserTest extends AbstractParserTest {

    private static final String MY_ASSOCIATION = "myAssociation";

    private static final String ANY_ASSOCIATION = "anyAssociation";

    @Mock
    private IPolicyCmptTypeAssociation assciation;

    @Mock
    private IPolicyCmptType policyCmptType;

    @Mock
    private IPolicyCmptType targetType;

    private AssociationParser associationParser;

    @Before
    public void createAssociationParser() throws Exception {
        associationParser = new AssociationParser(getExpression(), getIpsProject());
    }

    @Before
    public void mockAssociation() throws Exception {
        when(policyCmptType.findAssociation(MY_ASSOCIATION, getIpsProject())).thenReturn(assciation);
        when(assciation.getName()).thenReturn(MY_ASSOCIATION);
        when(assciation.findTarget(getIpsProject())).thenReturn(targetType);
    }

    @Test
    public void testParse_noAssociationWithoutPreviousNode() throws Exception {
        IdentifierNode node = associationParser.parse(MY_ASSOCIATION, null);

        assertNull(node);
    }

    @Test
    public void testParse_findAssociation1To1() throws Exception {
        when(assciation.is1ToMany()).thenReturn(false);

        AssociationNode node = (AssociationNode)associationParser.parse(MY_ASSOCIATION, new TestNode(policyCmptType));

        assertEquals(assciation, node.getAssociation());
        assertEquals(targetType, node.getDatatype());
    }

    @Test
    public void testParse_findAssociation1ToMany() throws Exception {
        when(assciation.is1ToMany()).thenReturn(true);

        AssociationNode node = (AssociationNode)associationParser.parse(MY_ASSOCIATION, new TestNode(policyCmptType));

        assertEquals(assciation, node.getAssociation());
        assertEquals(new ListOfTypeDatatype(targetType), node.getDatatype());
    }

    @Test
    public void testParse_findAssociation1To1FromMany() throws Exception {
        when(assciation.is1ToMany()).thenReturn(false);

        AssociationNode node = (AssociationNode)associationParser.parse(MY_ASSOCIATION, new TestNode(policyCmptType,
                true));

        assertEquals(assciation, node.getAssociation());
        assertEquals(new ListOfTypeDatatype(targetType), node.getDatatype());
    }

    @Test
    public void testParse_findAssociation1ToManyFromMany() throws Exception {
        when(assciation.is1ToMany()).thenReturn(true);

        AssociationNode node = (AssociationNode)associationParser.parse(MY_ASSOCIATION, new TestNode(policyCmptType,
                true));

        assertEquals(assciation, node.getAssociation());
        assertEquals(new ListOfTypeDatatype(targetType), node.getDatatype());
    }

    @Test
    public void testParse_findNoAssociation() throws Exception {
        AssociationNode node = (AssociationNode)associationParser.parse(ANY_ASSOCIATION, new TestNode(
                getProductCmptType()));

        assertNull(node);
    }

}
