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
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.core.builder.flidentifier.ast.AssociationNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.AttributeNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumClassNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumValueNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IndexBasedAssociationNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.ParameterNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.QualifiedAssociationNode;
import org.faktorips.devtools.core.internal.fl.IdentifierFilter;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.method.IFormulaMethod;
import org.faktorips.devtools.core.model.method.IParameter;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IdentifierParserTest {

    private static final String MY_PARAMETER = "anyParameter";

    private static final String MY_ATTRIBUTE = "myAttribute";

    private static final String MY_ASSOCIATION = "myAssociation";

    private static final String MY_ASSOCIATION_QUALIFIED = "myAssociation1[\"abc123\"]";

    private static final String MY_ASSOCIATION_INDEX = "myAssociation2[0]";

    private static final String MY_IDENTIFIER = MY_PARAMETER + '.' + MY_ASSOCIATION + '.' + MY_ASSOCIATION_QUALIFIED
            + '.' + MY_ASSOCIATION_INDEX + '.' + MY_ATTRIBUTE;

    private static final String MY_ENUMCLASS = "myEnumClas";

    private static final String MY_ENUMVALUE = "myEnumType";

    private IdentifierParser identifierParser;

    @Mock
    private IExpression expression;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IFormulaMethod formulaMethod;

    @Mock
    private IType type;

    @Mock
    private IType type1;

    @Mock
    private IType type2;

    @Mock
    private IType type3;

    @Mock
    private IProductCmptType productCmptType;

    @Mock
    private IParameter parameter;

    @Mock
    private IAttribute attribute;

    @Mock
    private IAssociation association;

    @Mock
    private IAssociation associationQualified;

    @Mock
    private IAssociation associationIndexed;

    @Mock
    private EnumDatatype enumDatatype;

    @Mock
    private IdentifierFilter identifierFilter;

    @Before
    public void createIdentifierParser() throws Exception {
        identifierParser = new IdentifierParser(expression, ipsProject, identifierFilter);
    }

    @Before
    public void mockExpression() throws Exception {
        when(expression.findProductCmptType(ipsProject)).thenReturn(productCmptType);
        when(expression.findFormulaSignature(ipsProject)).thenReturn(formulaMethod);
        when(formulaMethod.getParameters()).thenReturn(new IParameter[] { parameter });
        when(parameter.getName()).thenReturn(MY_PARAMETER);
        when(parameter.findDatatype(ipsProject)).thenReturn(type);
        when(type.findAssociation(MY_ASSOCIATION, ipsProject)).thenReturn(association);
        when(association.findTarget(ipsProject)).thenReturn(type1);
        when(association.is1ToMany()).thenReturn(true);
        when(type1.findAssociation(MY_ASSOCIATION + "1", ipsProject)).thenReturn(associationQualified);
        when(associationQualified.findTarget(ipsProject)).thenReturn(type2);
        when(type2.findAssociation(MY_ASSOCIATION + "2", ipsProject)).thenReturn(associationIndexed);
        when(associationIndexed.findTarget(ipsProject)).thenReturn(type3);
        when(type3.findAllAttributes(ipsProject)).thenReturn(Arrays.asList(attribute));
        when(attribute.getName()).thenReturn(MY_ATTRIBUTE);
        when(attribute.findDatatype(ipsProject)).thenReturn(Datatype.GREGORIAN_CALENDAR);
        when(identifierFilter.isIdentifierAllowed(any(IIpsObjectPartContainer.class))).thenReturn(true);
    }

    @Before
    public void mockEnum() throws Exception {
        when(expression.getEnumDatatypesAllowedInFormula()).thenReturn(new EnumDatatype[] { enumDatatype });
        when(enumDatatype.getName()).thenReturn(MY_ENUMCLASS);
        when(enumDatatype.getAllValueIds(true)).thenReturn(new String[] { MY_ENUMVALUE });
    }

    @Test
    public void testParse_illegalIdentifier() throws Exception {
        IdentifierNode identifierNode = identifierParser.parse("no" + MY_IDENTIFIER);

        assertTrue(identifierNode instanceof InvalidIdentifierNode);
    }

    @Test
    public void testParse_multiParametersAssociationsAttributes() throws Exception {
        ParameterNode parameterNode = (ParameterNode)identifierParser.parse(MY_IDENTIFIER);

        assertEquals(parameter, parameterNode.getParameter());
        AssociationNode associationNode = (AssociationNode)parameterNode.getSuccessor();
        assertEquals(association, associationNode.getAssociation());
        assertTrue(associationNode.getDatatype() instanceof ListOfTypeDatatype);
        QualifiedAssociationNode qualifiedAssociationNode = (QualifiedAssociationNode)associationNode.getSuccessor();
        assertEquals(associationQualified, qualifiedAssociationNode.getAssociation());
        assertEquals("abc123", qualifiedAssociationNode.getQualifier());
        assertTrue(qualifiedAssociationNode.getDatatype() instanceof ListOfTypeDatatype);
        IndexBasedAssociationNode indexBasedAssociationNode = (IndexBasedAssociationNode)qualifiedAssociationNode
                .getSuccessor();
        assertEquals(associationIndexed, indexBasedAssociationNode.getAssociation());
        assertEquals(0, indexBasedAssociationNode.getIndex());
        assertEquals(type3, indexBasedAssociationNode.getDatatype());
        AttributeNode attributeNode = (AttributeNode)indexBasedAssociationNode.getSuccessor();
        assertEquals(attribute, attributeNode.getAttribute());
        assertEquals(Datatype.GREGORIAN_CALENDAR, attributeNode.getDatatype());
    }

    @Test
    public void testParse_ListTypeForAttributes() throws Exception {
        when(type1.findAllAttributes(ipsProject)).thenReturn(Arrays.asList(attribute));

        ParameterNode parameterNode = (ParameterNode)identifierParser.parse(MY_PARAMETER + '.' + MY_ASSOCIATION + '.'
                + MY_ATTRIBUTE);

        assertEquals(parameter, parameterNode.getParameter());
        AssociationNode associationNode = (AssociationNode)parameterNode.getSuccessor();
        assertEquals(association, associationNode.getAssociation());
        assertTrue(associationNode.getDatatype() instanceof ListOfTypeDatatype);
        AttributeNode attributeNode = (AttributeNode)associationNode.getSuccessor();
        assertEquals(attribute, attributeNode.getAttribute());
        assertEquals(new ListOfTypeDatatype(Datatype.GREGORIAN_CALENDAR), attributeNode.getDatatype());
    }

    @Test
    public void testParse_multiEnum() throws Exception {
        EnumClassNode enumClassNode = (EnumClassNode)identifierParser.parse(MY_ENUMCLASS + '.' + MY_ENUMVALUE);

        assertEquals(enumDatatype, enumClassNode.getDatatype().getEnumDatatype());
        EnumValueNode enumDatatypeNode = (EnumValueNode)enumClassNode.getSuccessor();
        assertEquals(MY_ENUMVALUE, enumDatatypeNode.getEnumValueName());
        assertEquals(enumDatatype, enumDatatypeNode.getDatatype());
    }

}
