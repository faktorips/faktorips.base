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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierParser;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.internal.fl.IdentifierVisitor;
import org.faktorips.fl.parser.ASTAddNode;
import org.faktorips.fl.parser.ASTArgListNode;
import org.faktorips.fl.parser.ASTBooleanNode;
import org.faktorips.fl.parser.ASTDecimalNode;
import org.faktorips.fl.parser.ASTDivNode;
import org.faktorips.fl.parser.ASTEQNode;
import org.faktorips.fl.parser.ASTFunctionCallNode;
import org.faktorips.fl.parser.ASTGENode;
import org.faktorips.fl.parser.ASTGTNode;
import org.faktorips.fl.parser.ASTIdentifierNode;
import org.faktorips.fl.parser.ASTIntegerNode;
import org.faktorips.fl.parser.ASTLENode;
import org.faktorips.fl.parser.ASTLTNode;
import org.faktorips.fl.parser.ASTMinusNode;
import org.faktorips.fl.parser.ASTMoneyNode;
import org.faktorips.fl.parser.ASTMultNode;
import org.faktorips.fl.parser.ASTNotEQNode;
import org.faktorips.fl.parser.ASTNotNode;
import org.faktorips.fl.parser.ASTNullNode;
import org.faktorips.fl.parser.ASTParenthesisNode;
import org.faktorips.fl.parser.ASTPlusNode;
import org.faktorips.fl.parser.ASTStart;
import org.faktorips.fl.parser.ASTStringNode;
import org.faktorips.fl.parser.ASTSubNode;
import org.faktorips.fl.parser.SimpleNode;
import org.faktorips.fl.parser.Token;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class IdentifierVisitorTest {

    private static final int TOKEN_1_COL = 2;

    private static final int TOKEN_2_COL = 7;

    private static final String MY_IDENTIFIER_TEXT1 = "myIdentifierText";

    private static final String MY_IDENTIFIER_TEXT2 = "otherIdentifierText";

    @Mock
    private IdentifierParser identifierParser;

    @Mock
    private SimpleNode simpleNode;

    private ASTStart aSTStart;

    private ASTEQNode aSTEQNode;

    private ASTNotEQNode aSTNotEQNode;

    private ASTLTNode aSTLTNode;

    private ASTGTNode aSTGTNode;

    private ASTLENode aSTLENode;

    private ASTGENode aSTGENode;

    private ASTAddNode aSTAddNode;

    private ASTSubNode aSTSubNode;

    private ASTMultNode aSTMultNode;

    private ASTDivNode aSTDivNode;

    private ASTPlusNode aSTPlusNode;

    private ASTMinusNode aSTMinusNode;

    private ASTNotNode aSTNotNode;

    private ASTParenthesisNode aSTParenthesisNode;

    private ASTFunctionCallNode aSTFunctionCallNode;

    private ASTArgListNode aSTArgListNode;

    private ASTBooleanNode aSTBooleanNode;

    private ASTIntegerNode aSTIntegerNode;

    private ASTDecimalNode aSTDecimalNode;

    private ASTStringNode aSTStringNode;

    private ASTMoneyNode aSTMoneyNode;

    private ASTNullNode aSTNullNode;

    private ASTIdentifierNode aSTIdentifierNode1;

    private ASTIdentifierNode aSTIdentifierNode2;

    @Mock
    private IdentifierNode identifierNode1;

    @Mock
    private IdentifierNode identifierNode2;

    private IdentifierVisitor identifierVisitor;

    @Before
    public void setUpIdentifierVisitor() {
        identifierVisitor = new IdentifierVisitor("my expression", identifierParser);
    }

    @Before
    public void setUpNodes() {
        aSTIdentifierNode1 = spy(new ASTIdentifierNode(0));
        aSTIdentifierNode2 = spy(new ASTIdentifierNode(0));

        aSTNullNode = new ASTNullNode(0);
        aSTNullNode.jjtAddChild(aSTIdentifierNode1, 0);
        aSTNullNode.jjtAddChild(aSTIdentifierNode2, 1);

        aSTMoneyNode = new ASTMoneyNode(0);
        aSTMoneyNode.jjtAddChild(aSTNullNode, 0);

        aSTStringNode = new ASTStringNode(0);
        aSTStringNode.jjtAddChild(aSTMoneyNode, 0);

        aSTDecimalNode = new ASTDecimalNode(0);
        aSTDecimalNode.jjtAddChild(aSTStringNode, 0);

        aSTIntegerNode = new ASTIntegerNode(0);
        aSTIntegerNode.jjtAddChild(aSTDecimalNode, 0);

        aSTBooleanNode = new ASTBooleanNode(0);
        aSTBooleanNode.jjtAddChild(aSTIntegerNode, 0);

        aSTArgListNode = new ASTArgListNode(0);
        aSTArgListNode.jjtAddChild(aSTBooleanNode, 0);

        aSTFunctionCallNode = new ASTFunctionCallNode(0);
        aSTFunctionCallNode.jjtAddChild(aSTArgListNode, 0);

        aSTParenthesisNode = new ASTParenthesisNode(0);
        aSTParenthesisNode.jjtAddChild(aSTFunctionCallNode, 0);

        aSTNotNode = new ASTNotNode(0);
        aSTNotNode.jjtAddChild(aSTParenthesisNode, 0);

        aSTMinusNode = new ASTMinusNode(0);
        aSTMinusNode.jjtAddChild(aSTNotNode, 0);

        aSTPlusNode = new ASTPlusNode(0);
        aSTPlusNode.jjtAddChild(aSTMinusNode, 0);

        aSTDivNode = new ASTDivNode(0);
        aSTDivNode.jjtAddChild(aSTPlusNode, 0);

        aSTMultNode = new ASTMultNode(0);
        aSTMultNode.jjtAddChild(aSTDivNode, 0);

        aSTSubNode = new ASTSubNode(0);
        aSTSubNode.jjtAddChild(aSTMultNode, 0);

        aSTAddNode = new ASTAddNode(0);
        aSTAddNode.jjtAddChild(aSTSubNode, 0);

        aSTGENode = new ASTGENode(0);
        aSTGENode.jjtAddChild(aSTAddNode, 0);

        aSTLENode = new ASTLENode(0);
        aSTLENode.jjtAddChild(aSTGENode, 0);

        aSTGTNode = new ASTGTNode(0);
        aSTGTNode.jjtAddChild(aSTLENode, 0);

        aSTLTNode = new ASTLTNode(0);
        aSTLTNode.jjtAddChild(aSTGTNode, 0);

        aSTNotEQNode = new ASTNotEQNode(0);
        aSTNotEQNode.jjtAddChild(aSTLTNode, 0);

        aSTEQNode = new ASTEQNode(0);
        aSTEQNode.jjtAddChild(aSTNotEQNode, 0);

        aSTStart = new ASTStart(0);
        aSTStart.jjtAddChild(aSTEQNode, 0);

        setUpIdentifierToken();
    }

    public void setUpIdentifierToken() {
        Token newToken1 = Token.newToken(0);
        newToken1.image = MY_IDENTIFIER_TEXT1;
        when(aSTIdentifierNode1.getLastToken()).thenReturn(newToken1);
        newToken1.beginColumn = TOKEN_1_COL;
        Token newToken2 = Token.newToken(0);
        newToken2.image = MY_IDENTIFIER_TEXT2;
        when(aSTIdentifierNode2.getLastToken()).thenReturn(newToken2);
        newToken2.beginColumn = TOKEN_2_COL;
    }

    @Test
    public void testVisit() throws Exception {
        aSTStart.jjtAccept(identifierVisitor, null);

        verify(identifierParser).parse(MY_IDENTIFIER_TEXT1);
        verify(identifierParser).parse(MY_IDENTIFIER_TEXT2);
    }

    @Test
    public void testVisitASTIdentifierNode() throws Exception {
        when(identifierParser.parse(MY_IDENTIFIER_TEXT1)).thenReturn(identifierNode1);
        when(identifierParser.parse(MY_IDENTIFIER_TEXT2)).thenReturn(identifierNode2);

        aSTStart.jjtAccept(identifierVisitor, null);

        assertThat(identifierVisitor.getIdentifiers().keySet(), hasItem(identifierNode1));
        assertThat(identifierVisitor.getIdentifiers().keySet(), hasItem(identifierNode2));
        assertEquals(Integer.valueOf(TOKEN_1_COL - 1), identifierVisitor.getIdentifiers().get(identifierNode1));
        assertEquals(Integer.valueOf(TOKEN_2_COL - 1), identifierVisitor.getIdentifiers().get(identifierNode2));
    }

}
