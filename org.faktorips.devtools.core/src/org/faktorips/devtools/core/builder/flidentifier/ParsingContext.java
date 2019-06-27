/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.builder.flidentifier;

import java.util.LinkedList;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.MultiLanguageSupport;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IExpression;

/**
 * The context of the formula identifier parser. It includes information about the expression, the
 * current context and the already parsed identifier nodes.
 * 
 */
public class ParsingContext {

    private final IExpression expression;

    private final LinkedList<IdentifierNode> nodes = new LinkedList<IdentifierNode>();

    private final IIpsProject ipsProject;

    private final MultiLanguageSupport multiLanguageSupport;

    public ParsingContext(IExpression expression, IIpsProject ipsProject) {
        this(expression, ipsProject, IpsPlugin.getMultiLanguageSupport());
    }

    public ParsingContext(IExpression expression, IIpsProject ipsProject, MultiLanguageSupport multiLanguageSupport) {
        this.expression = expression;
        this.ipsProject = ipsProject;
        this.multiLanguageSupport = multiLanguageSupport;
    }

    public void init() {
        nodes.clear();
    }

    public void pushNode(IdentifierNode node) {
        nodes.push(node);
    }

    /**
     * Returns a linked list with already parsed nodes. The list is in reverse order that means the
     * latest node is at the begin of the list, the first node. If you iterate over the list you
     * first get the previous node.
     */
    public LinkedList<IdentifierNode> getNodes() {
        return new LinkedList<IdentifierNode>(nodes);
    }

    public IdentifierNode getPreviousNode() {
        return nodes.peek();
    }

    public IExpression getExpression() {
        return expression;
    }

    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    public MultiLanguageSupport getMultiLanguageSupport() {
        return multiLanguageSupport;
    }

}
