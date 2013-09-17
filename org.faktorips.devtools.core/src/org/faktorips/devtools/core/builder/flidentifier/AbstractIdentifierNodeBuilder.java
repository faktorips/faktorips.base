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

import org.faktorips.codegen.CodeFragment;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeType;
import org.faktorips.devtools.core.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.core.internal.fl.IdentifierFilter;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.fl.CompilationResult;

public abstract class AbstractIdentifierNodeBuilder<T extends CodeFragment> {

    public static final String DEFAULT_VALUE_SUFFIX = "@default"; //$NON-NLS-1$
    private final IdentifierNodeBuilderFactory<T> nodeBuilderFactory;

    public AbstractIdentifierNodeBuilder(IdentifierNodeBuilderFactory<T> factory) {
        this.nodeBuilderFactory = factory;
    }

    /**
     * Tests whether the given {@link IdentifierNode node} is an invalid node and returns an
     * {@link CompilationResult} containing error messages in that case. If the given given
     * {@link IdentifierNode node} is valid on the other hand, a {@link CompilationResult} with a
     * code fragment is built for it. This method also generates the {@link CompilationResult} of
     * the given node's successor (following identifier part), if there is one, by delegating to its
     * {@link #buildNode(IdentifierNode, CompilationResult)} method. The resulting
     * {@link CompilationResult} is returned.
     * 
     * @param identifierNode the node to process
     * @param contextCompilationResult the {@link CompilationResult} of the predecessor node. The
     *            context code is usually a statement this builder appends its node's code to (as in
     *            "contextCode.someFunction()") or code that is used as an argument for a function
     *            this builder generates (as in "someFunction(contextCode)").
     * @return The {@link CompilationResult} containing the complete code for the given identifier
     *         and all of its successors.
     * 
     * @see #getCompilationResult(IdentifierNode, CompilationResult)
     */
    public CompilationResult<T> buildNode(IdentifierNode identifierNode,
            CompilationResult<CodeFragment> contextCompilationResult) {
        if (isInvalidNode(identifierNode)) {
            return getErrorCompilationResult((InvalidIdentifierNode)identifierNode);
        }
        return buildNodeAndSuccessors(identifierNode, contextCompilationResult);
    }

    private CompilationResult<T> buildNodeAndSuccessors(IdentifierNode identifierNode,
            CompilationResult<CodeFragment> predecessorCompilationResult) {
        CompilationResult<T> compilationResult = getCompilationResult(identifierNode, predecessorCompilationResult);
        return getBuildSuccessorCompilationResultIfApplicable(identifierNode, compilationResult);
    }

    private CompilationResult<T> getBuildSuccessorCompilationResultIfApplicable(IdentifierNode identifierNode,
            CompilationResult<T> compilationResult) {
        if (identifierNode.hasSuccessor()) {
            IdentifierNode successorNode = identifierNode.getSuccessor();
            IdentifierNodeBuilder<T> successorBuilder = getBuilderFor(successorNode);
            return successorBuilder.buildNode(successorNode, compilationResult);
        } else {
            return compilationResult;
        }
    }

    protected IdentifierNodeBuilder<T> getBuilderFor(IdentifierNode node) {
        IdentifierNodeType nodeType = IdentifierNodeType.getNodeType(node.getClass());
        return nodeType.getBuilderFor(getNodeBuilderFactory());
    }

    /**
     * 
     * @param identifierNode the node this builder is currently processing. This node is always a
     *            "valid" node, never an InvalidNode.
     * @param contextCompilationResult The {@link CompilationResult} for the predecessor of the
     *            given {@link IdentifierNode}. The context code is usually a statement this builder
     *            appends its node's code to (as in "contextCode.someFunction()") or code that is
     *            used as an argument for a function this builder generates (as in
     *            "someFunction(contextCode)").
     * @return a {@link CompilationResult} containing the complete code this builder generates for
     *         the given {@link IdentifierNode}.
     */
    protected abstract CompilationResult<T> getCompilationResult(IdentifierNode identifierNode,
            CompilationResult<CodeFragment> contextCompilationResult);

    protected abstract CompilationResult<T> getErrorCompilationResult(InvalidIdentifierNode invalidIdentifierNode);

    private boolean isInvalidNode(IdentifierNode identifierNode) {
        return identifierNode.getClass() == IdentifierNodeType.INVALID_IDENTIFIER.getNodeClass();
    }

    protected boolean isIdentifierAllowed(IAttribute attribute) {
        return !getIdentifierFilter().isIdentifierAllowed(attribute);
    }

    protected IdentifierFilter getIdentifierFilter() {
        return IpsPlugin.getDefault().getIdentifierFilter();
    }

    protected IdentifierNodeBuilderFactory<T> getNodeBuilderFactory() {
        return nodeBuilderFactory;
    }
}
