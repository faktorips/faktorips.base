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

/*
 * Generated By:JJTree: Do not edit this line.
 * C:/projekte/ips/workspace/org.faktorips.fl/src/org/faktorips/fl/parser\FlParserVisitor.java
 */

package org.faktorips.fl.parser;

public interface FlParserVisitor {
    public Object visit(SimpleNode node, Object data);

    public Object visit(ASTStart node, Object data);

    public Object visit(ASTEQNode node, Object data);

    public Object visit(ASTNotEQNode node, Object data);

    public Object visit(ASTLTNode node, Object data);

    public Object visit(ASTGTNode node, Object data);

    public Object visit(ASTLENode node, Object data);

    public Object visit(ASTGENode node, Object data);

    public Object visit(ASTAddNode node, Object data);

    public Object visit(ASTSubNode node, Object data);

    public Object visit(ASTMultNode node, Object data);

    public Object visit(ASTDivNode node, Object data);

    public Object visit(ASTPlusNode node, Object data);

    public Object visit(ASTMinusNode node, Object data);

    public Object visit(ASTNotNode node, Object data);

    public Object visit(ASTParenthesisNode node, Object data);

    public Object visit(ASTFunctionCallNode node, Object data);

    public Object visit(ASTArgListNode node, Object data);

    public Object visit(ASTIdentifierNode node, Object data);

    public Object visit(ASTBooleanNode node, Object data);

    public Object visit(ASTIntegerNode node, Object data);

    public Object visit(ASTDecimalNode node, Object data);

    public Object visit(ASTStringNode node, Object data);

    public Object visit(ASTMoneyNode node, Object data);

    public Object visit(ASTNullNode node, Object data);
}
