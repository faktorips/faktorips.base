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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.core.builder.flidentifier.ast.AttributeNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.core.fl.IdentifierKind;
import org.faktorips.devtools.core.internal.fl.IdentifierFilter;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
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

    @Mock(extraInterfaces = IPolicyCmptTypeAttribute.class)
    private IAttribute attribute;

    @Mock
    private IAttribute attribute2;

    @Mock
    private IAttribute attribute3;

    @Mock
    private IType otherType;

    @Mock
    private IPolicyCmptType policyType;

    @Mock
    private IProductCmptType prodType;

    @Mock
    private IdentifierFilter identifierFilter;

    private AttributeParser attributeParser;

    @Before
    public void createAttributeParser() throws Exception {
        attributeParser = new AttributeParser(getParsingContext(), identifierFilter);
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
        getParsingContext().pushNode(new TestNode(getProductCmptType()));

        IdentifierNode attributeNode = attributeParser.parse(MY_ATTRIBUTE, null);

        assertNull(attributeNode);
    }

    @Test
    public void testParse_findAttributeInExpressionType() throws Exception {
        when(getExpression().findMatchingProductCmptTypeAttributes()).thenReturn(Arrays.asList(attribute));
        getParsingContext().pushNode(new TestNode(getProductCmptType()));

        AttributeNode attributeNode = (AttributeNode)attributeParser.parse(MY_ATTRIBUTE, null);

        assertEquals(attribute, attributeNode.getAttribute());
        assertFalse(attributeNode.isDefaultValueAccess());
    }

    @Test
    public void testParse_findAttributeInOtherType() throws Exception {
        when(otherType.findAllAttributes(getIpsProject())).thenReturn(Arrays.asList(attribute));
        getParsingContext().pushNode(new TestNode(otherType));

        AttributeNode attributeNode = (AttributeNode)attributeParser.parse(MY_ATTRIBUTE, null);

        assertEquals(attribute, attributeNode.getAttribute());
        assertFalse(attributeNode.isDefaultValueAccess());
    }

    @Test
    public void testParse_findAttributeInList() throws Exception {
        when(otherType.findAllAttributes(getIpsProject())).thenReturn(Arrays.asList(attribute));
        getParsingContext().pushNode(new TestNode(otherType, true));

        AttributeNode attributeNode = (AttributeNode)attributeParser.parse(MY_ATTRIBUTE, null);

        assertEquals(attribute, attributeNode.getAttribute());
        assertEquals(new ListOfTypeDatatype(Datatype.INTEGER), attributeNode.getDatatype());
        assertFalse(attributeNode.isDefaultValueAccess());
    }

    @Test
    public void testParse_findDefaultValueAccess() throws Exception {
        when(policyType.findAllAttributes(getIpsProject())).thenReturn(Arrays.asList(attribute));
        String identifierPart = MY_ATTRIBUTE + AttributeParser.DEFAULT_VALUE_SUFFIX;
        getParsingContext().pushNode(new TestNode(policyType));

        AttributeNode attributeNode = (AttributeNode)attributeParser.parse(identifierPart, null);

        assertEquals(attribute, attributeNode.getAttribute());
        assertTrue(attributeNode.isDefaultValueAccess());
    }

    @Test
    public void testParse_filteredAttribute() throws Exception {
        when(identifierFilter.isIdentifierAllowed(attribute, IdentifierKind.DEFAULT_IDENTIFIER)).thenReturn(false);
        when(policyType.findAllAttributes(getIpsProject())).thenReturn(Arrays.asList(attribute));
        String identifierPart = MY_ATTRIBUTE + AttributeParser.DEFAULT_VALUE_SUFFIX;
        getParsingContext().pushNode(new TestNode(policyType));

        InvalidIdentifierNode node = (InvalidIdentifierNode)attributeParser.parse(identifierPart, null);

        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, node.getMessage().getCode());
    }

    @Test
    public void testParse_noDatatype() throws Exception {
        when(attribute.findDatatype(getIpsProject())).thenReturn(null);
        when(policyType.findAllAttributes(getIpsProject())).thenReturn(Arrays.asList(attribute));
        String identifierPart = MY_ATTRIBUTE + AttributeParser.DEFAULT_VALUE_SUFFIX;
        getParsingContext().pushNode(new TestNode(policyType));

        InvalidIdentifierNode node = (InvalidIdentifierNode)attributeParser.parse(identifierPart, null);

        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, node.getMessage().getCode());
    }

    @Test
    public void testfindAttributes() throws CoreException {
        AttributeParser spy = spy(attributeParser);
        ArrayList<IAttribute> arrayList = new ArrayList<IAttribute>();
        arrayList.add(attribute);
        doReturn(false).when(spy).isContextTypeFormulaType();
        when(spy.getContextType()).thenReturn(policyType);
        when(policyType.findAllAttributes(getIpsProject())).thenReturn(arrayList);
        when(policyType.findProductCmptType(getIpsProject())).thenReturn(prodType);

        when(prodType.findAllAttributes(getIpsProject())).thenReturn(listOfAttributes());

        List<IAttribute> attributeList = spy.findAttributes();
        assertEquals(3, attributeList.size());
    }

    @Test
    public void testfindAttributes_NoProductCmpt() throws CoreException {
        AttributeParser spy = spy(attributeParser);
        doReturn(false).when(spy).isContextTypeFormulaType();
        when(spy.getContextType()).thenReturn(policyType);
        when(policyType.findAllAttributes(getIpsProject())).thenReturn(Arrays.asList(attribute));
        when(prodType.findAllAttributes(getIpsProject())).thenReturn(listOfAttributes());

        List<IAttribute> attributeList = spy.findAttributes();
        assertEquals(1, attributeList.size());
    }

    private ArrayList<IAttribute> listOfAttributes() {
        ArrayList<IAttribute> list = new ArrayList<IAttribute>();
        list.add(attribute2);
        list.add(attribute3);
        return list;
    }

    @Test
    public void testGetProposals() throws Exception {
        AttributeParser parser = mockAttributesForProposal();

        List<IdentifierNode> proposals = parser.getProposals(StringUtils.EMPTY);

        assertEquals(4, proposals.size());
        assertEquals(attribute, ((AttributeNode)proposals.get(0)).getAttribute());
        assertFalse(((AttributeNode)proposals.get(0)).isDefaultValueAccess());
        assertEquals(attribute, ((AttributeNode)proposals.get(1)).getAttribute());
        assertTrue(((AttributeNode)proposals.get(1)).isDefaultValueAccess());
        assertEquals(attribute2, ((AttributeNode)proposals.get(2)).getAttribute());
        assertEquals(attribute3, ((AttributeNode)proposals.get(3)).getAttribute());
    }

    @Test
    public void testGetProposals_withPrefix() throws Exception {
        AttributeParser parser = mockAttributesForProposal();

        List<IdentifierNode> proposals = parser.getProposals("my");

        assertEquals(3, proposals.size());
        assertEquals(attribute, ((AttributeNode)proposals.get(0)).getAttribute());
        assertFalse(((AttributeNode)proposals.get(0)).isDefaultValueAccess());
        assertEquals(attribute, ((AttributeNode)proposals.get(1)).getAttribute());
        assertTrue(((AttributeNode)proposals.get(1)).isDefaultValueAccess());
        assertEquals(attribute3, ((AttributeNode)proposals.get(2)).getAttribute());
    }

    @Test
    public void testGetProposals_withFilter() throws Exception {
        AttributeParser parser = mockAttributesForProposal();
        when(identifierFilter.isIdentifierAllowed(attribute, IdentifierKind.ATTRIBUTE)).thenReturn(false);
        when(identifierFilter.isIdentifierAllowed(attribute, IdentifierKind.DEFAULT_IDENTIFIER)).thenReturn(true);
        when(identifierFilter.isIdentifierAllowed(attribute2, IdentifierKind.ATTRIBUTE)).thenReturn(true);
        when(identifierFilter.isIdentifierAllowed(attribute3, IdentifierKind.ATTRIBUTE)).thenReturn(false);

        List<IdentifierNode> proposals = parser.getProposals(StringUtils.EMPTY);

        assertEquals(2, proposals.size());
        assertEquals(attribute, ((AttributeNode)proposals.get(0)).getAttribute());
        assertTrue(((AttributeNode)proposals.get(0)).isDefaultValueAccess());
        assertEquals(attribute2, ((AttributeNode)proposals.get(1)).getAttribute());
        assertFalse(((AttributeNode)proposals.get(1)).isDefaultValueAccess());
    }

    private AttributeParser mockAttributesForProposal() throws CoreException {
        AttributeParser spy = spy(attributeParser);
        doReturn(false).when(spy).isContextTypeFormulaType();
        when(spy.getContextType()).thenReturn(policyType);
        when(policyType.findAllAttributes(getIpsProject())).thenReturn(Arrays.asList(attribute));
        when(prodType.findAllAttributes(getIpsProject())).thenReturn(listOfAttributes());
        when(policyType.findProductCmptType(getIpsProject())).thenReturn(prodType);
        when(attribute2.getName()).thenReturn("xyz");
        when(attribute2.findDatatype(getIpsProject())).thenReturn(Datatype.INTEGER);
        when(attribute3.getName()).thenReturn("myProd");
        when(attribute3.findDatatype(getIpsProject())).thenReturn(Datatype.INTEGER);
        return spy;
    }

}
