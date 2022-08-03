/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.fl;

import java.util.HashMap;
import java.util.Map;

import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierParser;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
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
import org.faktorips.fl.parser.FlParserVisitor;
import org.faktorips.fl.parser.SimpleNode;

/**
 * This implementation of {@link FlParserVisitor} visits the parsed AST and finds
 * {@link ASTIdentifierNode identifiers} used in the parsed expression. The visitor further parses
 * these identifiers using an {@link IdentifierParser} and remembers, the parsed
 * {@link IdentifierNode} returned from {@link IdentifierParser} and the starting point of the
 * {@link ASTIdentifierNode} Within the expression text.
 * <p>
 * After creating the {@link IdentifierVisitor} you could use it by providing this visitor to
 * {@link ASTStart#jjtAccept(FlParserVisitor, Object)}. After that visiting the method
 * {@link #getIdentifiers()} should return the map of found identifiers.
 * 
 */
public class IdentifierVisitor implements FlParserVisitor {

    private Map<IdentifierNode, Integer> identifiers = new HashMap<>();

    private final String expressionText;

    private final IdentifierParser identifierParser;

    /**
     * Creating a new {@link IdentifierVisitor} that uses the given {@link IdentifierParser} to
     * parse found {@link ASTIdentifierNode}.
     * 
     * @param expressionText The text of the formula expression
     * @param identifierParser the {@link IdentifierParser} to process the found identifier
     */
    public IdentifierVisitor(String expressionText, IdentifierParser identifierParser) {
        this.expressionText = expressionText;
        this.identifierParser = identifierParser;
    }

    /**
     * After the visitor was accepted by the AST nodes, this method returns a map containing all
     * found and parsed identifiers and the corresponding starting position of the identifier.
     * 
     * @return The map of parsed {@link IdentifierNode} and the corresponding starting point.
     */
    public Map<IdentifierNode, Integer> getIdentifiers() {
        return identifiers;
    }

    @Override
    public Object visit(SimpleNode node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTIdentifierNode node, Object data) {
        String identifier = node.getLastToken().toString();
        IdentifierNode identifierNode = identifierParser.parse(identifier);
        getIdentifiers().put(identifierNode, getIdentifierOffset(node));
        return visit((SimpleNode)node, data);
    }

    /**
     * Returns the starting point of the identifier token. The AST only provides the row and column
     * of the starting point so we need to transfer these coordinates into a text position.
     * 
     * @param node The node of which we like to get the start of the identifier
     * @return the position of the start of the identifier within the expression text
     */
    int getIdentifierOffset(SimpleNode node) {
        return node.getLastToken().getStartPositionRelativeTo(expressionText);
    }

    // -----------------------------------------------------------------------------------------
    // All visit methods below this point simply delegate to visit(SimpleNode node, Object data)
    // -----------------------------------------------------------------------------------------

    @Override
    public Object visit(ASTStart node, Object data) {
        return visit((SimpleNode)node, data);
    }

    @Override
    public Object visit(ASTEQNode node, Object data) {
        return visit((SimpleNode)node, data);
    }

    @Override
    public Object visit(ASTNotEQNode node, Object data) {
        return visit((SimpleNode)node, data);
    }

    @Override
    public Object visit(ASTLTNode node, Object data) {
        return visit((SimpleNode)node, data);
    }

    @Override
    public Object visit(ASTGTNode node, Object data) {
        return visit((SimpleNode)node, data);
    }

    @Override
    public Object visit(ASTLENode node, Object data) {
        return visit((SimpleNode)node, data);
    }

    @Override
    public Object visit(ASTGENode node, Object data) {
        return visit((SimpleNode)node, data);
    }

    @Override
    public Object visit(ASTAddNode node, Object data) {
        return visit((SimpleNode)node, data);
    }

    @Override
    public Object visit(ASTSubNode node, Object data) {
        return visit((SimpleNode)node, data);
    }

    @Override
    public Object visit(ASTMultNode node, Object data) {
        return visit((SimpleNode)node, data);
    }

    @Override
    public Object visit(ASTDivNode node, Object data) {
        return visit((SimpleNode)node, data);
    }

    @Override
    public Object visit(ASTPlusNode node, Object data) {
        return visit((SimpleNode)node, data);
    }

    @Override
    public Object visit(ASTMinusNode node, Object data) {
        return visit((SimpleNode)node, data);
    }

    @Override
    public Object visit(ASTNotNode node, Object data) {
        return visit((SimpleNode)node, data);
    }

    @Override
    public Object visit(ASTParenthesisNode node, Object data) {
        return visit((SimpleNode)node, data);
    }

    @Override
    public Object visit(ASTFunctionCallNode node, Object data) {
        return visit((SimpleNode)node, data);
    }

    @Override
    public Object visit(ASTArgListNode node, Object data) {
        return visit((SimpleNode)node, data);
    }

    @Override
    public Object visit(ASTBooleanNode node, Object data) {
        return visit((SimpleNode)node, data);
    }

    @Override
    public Object visit(ASTIntegerNode node, Object data) {
        return visit((SimpleNode)node, data);
    }

    @Override
    public Object visit(ASTDecimalNode node, Object data) {
        return visit((SimpleNode)node, data);
    }

    @Override
    public Object visit(ASTStringNode node, Object data) {
        return visit((SimpleNode)node, data);
    }

    @Override
    public Object visit(ASTMoneyNode node, Object data) {
        return visit((SimpleNode)node, data);
    }

    @Override
    public Object visit(ASTNullNode node, Object data) {
        return visit((SimpleNode)node, data);
    }

}
