/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.QualifierNode;
import org.faktorips.devtools.model.internal.dependency.IpsObjectDependency;
import org.faktorips.devtools.model.internal.fl.IdentifierVisitor;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.productcmpt.IExpressionDependencyDetail;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.util.TextRegion;
import org.faktorips.fl.parser.ASTAddNode;
import org.faktorips.fl.parser.ASTIntegerNode;
import org.faktorips.fl.parser.ASTStart;
import org.faktorips.fl.parser.Node;
import org.faktorips.fl.parser.SimpleNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExpressionDependencyCollectorTest {

    private static final String MY_EXPRESSION = "1 + 2";

    private static final String MY_TARGET_NAME = "myTargetName";

    private static final String MY_NAME = "myName";

    @Mock
    private IIpsObjectPartContainer parent;

    @Mock
    private IdentifierVisitor identifierVisitor;

    @Mock
    private SimpleNode simpleNode;

    @Mock
    private IdentifierNode identifierNode;

    @Mock
    private QualifierNode qualifierNode;

    @Mock
    private QualifierNode qualifierNode2;

    private Map<IdentifierNode, Integer> identifiers;

    @Mock
    private IIpsObject ipsObject;

    @Mock
    private IProductCmpt targetProductCmpt;

    @Mock
    private IProductCmpt targetProductCmpt2;
    @Mock
    private Expression expression;

    private ExpressionDependencyCollector expressionDependencyCollector;

    @Before
    public void setUpIdentifierVisitor() {
        identifiers = new HashMap<>();
        when(identifierVisitor.getIdentifiers()).thenReturn(identifiers);
    }

    @Before
    public void setUpExpression() {
        when(expression.getIpsObject()).thenReturn(ipsObject);
        when(expression.getEnumDatatypesAllowedInFormula()).thenReturn(new EnumDatatype[] {});
        when(ipsObject.getQualifiedNameType()).thenReturn(new QualifiedNameType(MY_NAME, IpsObjectType.PRODUCT_CMPT));
    }

    @Before
    public void createExpressionDependencyCollector() throws Exception {
        expressionDependencyCollector = new ExpressionDependencyCollector(expression, identifierVisitor);
    }

    @Before
    public void setUpQualifiedNodes() {
        when(qualifierNode.getProductCmpt()).thenReturn(targetProductCmpt);
        when(targetProductCmpt.getQualifiedNameType()).thenReturn(
                new QualifiedNameType(MY_TARGET_NAME, IpsObjectType.PRODUCT_CMPT));
        when(qualifierNode.getTextRegion()).thenReturn(new TextRegion(MY_EXPRESSION, 1, 7));
        when(qualifierNode2.getProductCmpt()).thenReturn(targetProductCmpt2);
        when(targetProductCmpt2.getQualifiedNameType()).thenReturn(
                new QualifiedNameType("secondTarget", IpsObjectType.PRODUCT_CMPT));
        when(qualifierNode2.getTextRegion()).thenReturn(new TextRegion(MY_EXPRESSION, 8, 29));
    }

    @Test
    public void testCollectDependencies_noParsingResult() throws Exception {
        when(expression.getExpression()).thenReturn("");

        Map<IDependency, IExpressionDependencyDetail> dependencies = expressionDependencyCollector
                .collectDependencies();

        assertTrue(dependencies.isEmpty());
    }

    @Test
    public void testCollectDependencies_noDependencies() throws Exception {
        Map<IDependency, IExpressionDependencyDetail> dependencies = expressionDependencyCollector
                .collectDependencies(simpleNode);

        assertTrue(dependencies.isEmpty());
        verify(simpleNode).jjtAccept(identifierVisitor, null);
        verify(identifierVisitor).getIdentifiers();
    }

    @Test
    public void testCollectDependencies_anyDependencies() throws Exception {
        identifiers.put(qualifierNode, 0);

        Map<IDependency, IExpressionDependencyDetail> dependencies = expressionDependencyCollector
                .collectDependencies(simpleNode);

        IDependency dependency = getDependency(targetProductCmpt.getQualifiedNameType());
        assertThat(dependencies.keySet(), hasItem(dependency));
    }

    @Test
    public void testCollectDependencies_someDependencies() throws Exception {
        identifiers.put(qualifierNode, 0);
        identifiers.put(qualifierNode2, 0);

        Map<IDependency, IExpressionDependencyDetail> dependencies = expressionDependencyCollector
                .collectDependencies(simpleNode);

        IDependency dependency = getDependency(targetProductCmpt.getQualifiedNameType());
        assertThat(dependencies.keySet(), hasItem(dependency));
        IDependency dependency2 = getDependency(targetProductCmpt2.getQualifiedNameType());
        assertThat(dependencies.keySet(), hasItem(dependency2));
    }

    @Test
    public void testCollectDependencies_simpleQualifiedNode() throws Exception {
        expressionDependencyCollector.collectDependencies(qualifierNode, 0);
        Map<IDependency, IExpressionDependencyDetail> dependencies = expressionDependencyCollector.getResult();

        IDependency dependency = getDependency(targetProductCmpt.getQualifiedNameType());
        assertThat(dependencies.keySet(), hasItem(dependency));
    }

    @Test
    public void testCollectDependencies_oneNestedNode() throws Exception {
        when(identifierNode.getSuccessor()).thenReturn(qualifierNode);
        when(identifierNode.hasSuccessor()).thenReturn(true);

        expressionDependencyCollector.collectDependencies(identifierNode, 0);
        Map<IDependency, IExpressionDependencyDetail> dependencies = expressionDependencyCollector.getResult();

        IDependency dependency = getDependency(targetProductCmpt.getQualifiedNameType());
        assertThat(dependencies.keySet(), hasItem(dependency));
    }

    @Test
    public void testCollectDependencies_someNestedNode() throws Exception {
        when(identifierNode.getSuccessor()).thenReturn(qualifierNode);
        when(identifierNode.hasSuccessor()).thenReturn(true);
        when(qualifierNode.getSuccessor()).thenReturn(qualifierNode2);
        when(qualifierNode.hasSuccessor()).thenReturn(true);

        expressionDependencyCollector.collectDependencies(identifierNode, 0);
        Map<IDependency, IExpressionDependencyDetail> dependencies = expressionDependencyCollector.getResult();

        IDependency dependency = getDependency(targetProductCmpt.getQualifiedNameType());
        assertThat(dependencies.keySet(), hasItem(dependency));
        IDependency dependency2 = getDependency(targetProductCmpt2.getQualifiedNameType());
        assertThat(dependencies.keySet(), hasItem(dependency2));
    }

    @Test
    public void testCollectDependencies_detailOneRegion() throws Exception {

        expressionDependencyCollector.collectDependencies(qualifierNode, 42);
        Map<IDependency, IExpressionDependencyDetail> dependencies = expressionDependencyCollector.getResult();
        IDependency dependency = getDependency(targetProductCmpt.getQualifiedNameType());
        ExpressionDependencyDetail expressionDependencyDetail = (ExpressionDependencyDetail)dependencies
                .get(dependency);

        assertEquals(1, expressionDependencyDetail.getTextRegions().size());
        assertEquals(new TextRegion(MY_EXPRESSION, 44, 47), expressionDependencyDetail.getTextRegions().first());
    }

    @Test
    public void testCollectDependencies_detailTwoRegion() throws Exception {
        when(qualifierNode.getSuccessor()).thenReturn(qualifierNode2);
        when(qualifierNode.hasSuccessor()).thenReturn(true);
        when(qualifierNode2.getProductCmpt()).thenReturn(targetProductCmpt);

        expressionDependencyCollector.collectDependencies(qualifierNode, 42);
        Map<IDependency, IExpressionDependencyDetail> dependencies = expressionDependencyCollector.getResult();
        IDependency dependency = getDependency(targetProductCmpt.getQualifiedNameType());
        ExpressionDependencyDetail expressionDependencyDetail = (ExpressionDependencyDetail)dependencies
                .get(dependency);

        assertEquals(2, expressionDependencyDetail.getTextRegions().size());
        assertEquals(new TextRegion(MY_EXPRESSION, 44, 47), expressionDependencyDetail.getTextRegions().first());
        assertEquals(new TextRegion(MY_EXPRESSION, 51, 69), expressionDependencyDetail.getTextRegions().last());
    }

    @Test
    public void testGetTextRegion() throws Exception {
        when(identifierNode.getTextRegion()).thenReturn(new TextRegion(MY_EXPRESSION, 3, 8));

        TextRegion textRegion = expressionDependencyCollector.getTextRegion(identifierNode, 42, 13, 9);

        assertEquals(58, textRegion.getStart());
        assertEquals(59, textRegion.getEnd());
    }

    private IDependency getDependency(QualifiedNameType target) {
        return IpsObjectDependency.createReferenceDependency(expression.getIpsObject().getQualifiedNameType(), target);
    }

    @Test
    public void testParseExpression() {
        when(expression.getExpression()).thenReturn(MY_EXPRESSION);

        SimpleNode node = expressionDependencyCollector.parseExpression();

        assertThat(node, instanceOf(ASTStart.class));
        assertThat(node.jjtGetNumChildren(), is(1));
        Node addNode = node.jjtGetChild(0);
        assertThat(addNode, instanceOf(ASTAddNode.class));
        assertThat(addNode.jjtGetNumChildren(), is(2));
        assertThat(addNode.jjtGetChild(0), instanceOf(ASTIntegerNode.class));
        assertThat(addNode.jjtGetChild(1), instanceOf(ASTIntegerNode.class));
    }

    @Test
    public void testParseExpression_withError() throws Exception {
        when(expression.getExpression()).thenReturn("a..b");

        SimpleNode node = expressionDependencyCollector.parseExpression();

        assertNull(node);
    }

}
