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
