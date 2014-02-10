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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.core.builder.flidentifier.ast.AttributeNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.core.fl.IdentifierKind;
import org.faktorips.devtools.core.internal.fl.IdentifierFilter;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.fl.ExprCompiler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AttributeParserTest extends AbstractParserTest {

    private static final String MY_ATTRIBUTE = "myAttribute";

    @Mock
    private IAttribute attribute;

    @Mock
    private IType otherType;

    @Mock
    private IPolicyCmptType policyType;

    @Mock
    private IdentifierFilter identifierFilter;

    private AttributeParser attributeParser;

    @Before
    public void createAttributeParser() throws Exception {
        attributeParser = new AttributeParser(getExpression(), getIpsProject(), identifierFilter);
    }

    @Before
    public void mockAttribute() throws Exception {
        when(attribute.getName()).thenReturn(MY_ATTRIBUTE);
        when(attribute.findDatatype(getIpsProject())).thenReturn(Datatype.INTEGER);
        when(identifierFilter.isIdentifierAllowed(any(IIpsObjectPartContainer.class), any(IdentifierKind.class)))
                .thenReturn(true);
    }

    @Test
    public void testParse_noAttribute() throws Exception {
        IdentifierNode attributeNode = attributeParser.parse(MY_ATTRIBUTE, new TestNode(getProductCmptType()));

        assertNull(attributeNode);
    }

    @Test
    public void testParse_findAttributeInExpressionType() throws Exception {
        when(getExpression().findMatchingProductCmptTypeAttributes()).thenReturn(Arrays.asList(attribute));

        AttributeNode attributeNode = (AttributeNode)attributeParser.parse(MY_ATTRIBUTE, new TestNode(
                getProductCmptType()));

        assertEquals(attribute, attributeNode.getAttribute());
        assertFalse(attributeNode.isDefaultValueAccess());
    }

    @Test
    public void testParse_findAttributeInOtherType() throws Exception {
        when(otherType.findAllAttributes(getIpsProject())).thenReturn(Arrays.asList(attribute));

        AttributeNode attributeNode = (AttributeNode)attributeParser.parse(MY_ATTRIBUTE, new TestNode(otherType));

        assertEquals(attribute, attributeNode.getAttribute());
        assertFalse(attributeNode.isDefaultValueAccess());
    }

    @Test
    public void testParse_findAttributeInList() throws Exception {
        when(otherType.findAllAttributes(getIpsProject())).thenReturn(Arrays.asList(attribute));

        AttributeNode attributeNode = (AttributeNode)attributeParser.parse(MY_ATTRIBUTE, new TestNode(otherType, true));

        assertEquals(attribute, attributeNode.getAttribute());
        assertEquals(new ListOfTypeDatatype(Datatype.INTEGER), attributeNode.getDatatype());
        assertFalse(attributeNode.isDefaultValueAccess());
    }

    @Test
    public void testParse_findDefaultValueAccess() throws Exception {
        when(policyType.findAllAttributes(getIpsProject())).thenReturn(Arrays.asList(attribute));
        String identifierPart = MY_ATTRIBUTE + AttributeParser.DEFAULT_VALUE_SUFFIX;

        AttributeNode attributeNode = (AttributeNode)attributeParser.parse(identifierPart, new TestNode(policyType));

        assertEquals(attribute, attributeNode.getAttribute());
        assertTrue(attributeNode.isDefaultValueAccess());
    }

    @Test
    public void testParse_filteredAttribute() throws Exception {
        when(identifierFilter.isIdentifierAllowed(attribute, IdentifierKind.DEFAULT_IDENTIFIER)).thenReturn(false);
        when(policyType.findAllAttributes(getIpsProject())).thenReturn(Arrays.asList(attribute));
        String identifierPart = MY_ATTRIBUTE + AttributeParser.DEFAULT_VALUE_SUFFIX;

        InvalidIdentifierNode node = (InvalidIdentifierNode)attributeParser.parse(identifierPart, new TestNode(
                policyType));

        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, node.getMessage().getCode());
    }

    @Test
    public void testParse_noDatatype() throws Exception {
        when(attribute.findDatatype(getIpsProject())).thenReturn(null);
        when(policyType.findAllAttributes(getIpsProject())).thenReturn(Arrays.asList(attribute));
        String identifierPart = MY_ATTRIBUTE + AttributeParser.DEFAULT_VALUE_SUFFIX;

        InvalidIdentifierNode node = (InvalidIdentifierNode)attributeParser.parse(identifierPart, new TestNode(
                policyType));

        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, node.getMessage().getCode());
    }

}
