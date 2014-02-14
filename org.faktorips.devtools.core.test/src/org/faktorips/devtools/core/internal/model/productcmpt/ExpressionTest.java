/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.QualifierNode;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IDependencyDetail;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.fl.parser.ASTAddNode;
import org.faktorips.fl.parser.ASTIdentifierNode;
import org.faktorips.fl.parser.ASTIntegerNode;
import org.faktorips.fl.parser.ASTStart;
import org.faktorips.fl.parser.Node;
import org.faktorips.fl.parser.SimpleNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExpressionTest {

    private static final String MY_TARGET_NAME = "myTargetName";

    private static final String MY_NAME = "myName";

    private static final String MY_EXPRESSION = "1 + 2";

    @Mock
    private IIpsObjectPartContainer parent;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private Expression expression;

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

    @Mock
    private ASTIdentifierNode astIdentifierNode;

    private Map<IdentifierNode, SimpleNode> identifiers;

    @Mock
    private IIpsObject ipsObject;

    @Mock
    private IProductCmpt targetProductCmpt;

    @Mock
    private IProductCmpt targetProductCmpt2;

    @Before
    public void setUpExpression() {
        when(expression.getIpsObject()).thenReturn(ipsObject);
        when(ipsObject.getQualifiedNameType()).thenReturn(new QualifiedNameType(MY_NAME, IpsObjectType.PRODUCT_CMPT));
    }

    @Before
    public void setUpIdentifierVisitor() {
        identifiers = new HashMap<IdentifierNode, SimpleNode>();
        doReturn(identifierVisitor).when(expression).newIdentifierVisitor();
        when(identifierVisitor.getIdentifiers()).thenReturn(identifiers);
    }

    @Before
    public void setUpQualifiedNodes() {
        when(qualifierNode.getProductCmpt()).thenReturn(targetProductCmpt);
        when(targetProductCmpt.getQualifiedNameType()).thenReturn(
                new QualifiedNameType(MY_TARGET_NAME, IpsObjectType.PRODUCT_CMPT));
        when(qualifierNode2.getProductCmpt()).thenReturn(targetProductCmpt2);
        when(targetProductCmpt2.getQualifiedNameType()).thenReturn(
                new QualifiedNameType("secondTarget", IpsObjectType.PRODUCT_CMPT));
    }

    @Test
    public void testDependsOn_noDependencies() throws Exception {
        doReturn(simpleNode).when(expression).parseExpression();

        Map<IDependency, List<IDependencyDetail>> dependsOn = expression.dependsOn();

        assertTrue(dependsOn.isEmpty());
        verify(simpleNode).jjtAccept(identifierVisitor, null);
        verify(identifierVisitor).getIdentifiers();
    }

    @Test
    public void testDependsOn_anyDependencies() throws Exception {
        doReturn(simpleNode).when(expression).parseExpression();
        identifiers.put(qualifierNode, astIdentifierNode);

        Map<IDependency, List<IDependencyDetail>> dependencies = expression.dependsOn();

        IDependency dependency = getDependency(targetProductCmpt.getQualifiedNameType());
        assertThat(dependencies.keySet(), hasItem(dependency));
    }

    @Test
    public void testDependsOn_someDependencies() throws Exception {
        doReturn(simpleNode).when(expression).parseExpression();
        identifiers.put(qualifierNode, astIdentifierNode);
        identifiers.put(qualifierNode2, astIdentifierNode);

        Map<IDependency, List<IDependencyDetail>> dependencies = expression.dependsOn();

        IDependency dependency = getDependency(targetProductCmpt.getQualifiedNameType());
        assertThat(dependencies.keySet(), hasItem(dependency));
        IDependency dependency2 = getDependency(targetProductCmpt2.getQualifiedNameType());
        assertThat(dependencies.keySet(), hasItem(dependency2));
    }

    private IDependency getDependency(QualifiedNameType target) {
        return IpsObjectDependency.createReferenceDependency(expression.getIpsObject().getQualifiedNameType(), target);
    }

    @Test
    public void testGetDependencies_simpleQualifiedNode() throws Exception {
        Map<IDependency, List<IDependencyDetail>> dependencies = expression.getDependencies(qualifierNode);

        IDependency dependency = getDependency(targetProductCmpt.getQualifiedNameType());
        assertThat(dependencies.keySet(), hasItem(dependency));
    }

    @Test
    public void testGetDependencies_oneNestedNode() throws Exception {
        when(identifierNode.getSuccessor()).thenReturn(qualifierNode);
        when(identifierNode.hasSuccessor()).thenReturn(true);

        Map<IDependency, List<IDependencyDetail>> dependencies = expression.getDependencies(identifierNode);

        IDependency dependency = getDependency(targetProductCmpt.getQualifiedNameType());
        assertThat(dependencies.keySet(), hasItem(dependency));
    }

    @Test
    public void testGetDependencies_someNestedNode() throws Exception {
        when(identifierNode.getSuccessor()).thenReturn(qualifierNode);
        when(identifierNode.hasSuccessor()).thenReturn(true);
        when(qualifierNode.getSuccessor()).thenReturn(qualifierNode2);
        when(qualifierNode.hasSuccessor()).thenReturn(true);

        Map<IDependency, List<IDependencyDetail>> dependencies = expression.getDependencies(identifierNode);

        IDependency dependency = getDependency(targetProductCmpt.getQualifiedNameType());
        assertThat(dependencies.keySet(), hasItem(dependency));
        IDependency dependency2 = getDependency(targetProductCmpt2.getQualifiedNameType());
        assertThat(dependencies.keySet(), hasItem(dependency2));
    }

    @Test
    public void testParseExpression() throws Exception {
        when(expression.getExpression()).thenReturn(MY_EXPRESSION);

        SimpleNode node = expression.parseExpression();

        assertThat(node, instanceOf(ASTStart.class));
        assertThat(node.jjtGetNumChildren(), is(1));
        Node addNode = node.jjtGetChild(0);
        assertThat(addNode, instanceOf(ASTAddNode.class));
        assertThat(addNode.jjtGetNumChildren(), is(2));
        assertThat(addNode.jjtGetChild(0), instanceOf(ASTIntegerNode.class));
        assertThat(addNode.jjtGetChild(1), instanceOf(ASTIntegerNode.class));
    }

}
