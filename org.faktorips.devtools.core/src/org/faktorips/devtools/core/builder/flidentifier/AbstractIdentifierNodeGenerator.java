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
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeType;
import org.faktorips.devtools.core.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.fl.CompilationResult;

public abstract class AbstractIdentifierNodeGenerator<T extends CodeFragment> implements IdentifierNodeGenerator<T> {

    public static final String DEFAULT_VALUE_SUFFIX = "@default"; //$NON-NLS-1$
    private final IdentifierNodeGeneratorFactory<T> nodeBuilderFactory;

    public AbstractIdentifierNodeGenerator(IdentifierNodeGeneratorFactory<T> factory) {
        this.nodeBuilderFactory = factory;
    }

    /**
     * Tests whether the given {@link IdentifierNode node} is an invalid node and returns an
     * {@link CompilationResult} containing error messages in that case. If the given given
     * {@link IdentifierNode node} is valid on the other hand, a {@link CompilationResult} with a
     * code fragment is built for it. This method also generates the {@link CompilationResult} of
     * the given node's successor (following identifier part), if there is one, by delegating to its
     * {@link #generateNode(IdentifierNode, CompilationResult)} method. The resulting
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
    @Override
    public CompilationResult<T> generateNode(IdentifierNode identifierNode,
            CompilationResult<T> contextCompilationResult) {
        if (isInvalidNode(identifierNode)) {
            return getErrorCompilationResult((InvalidIdentifierNode)identifierNode);
        }
        return generateNodeAndSuccessors(identifierNode, contextCompilationResult);
    }

    private CompilationResult<T> generateNodeAndSuccessors(IdentifierNode identifierNode,
            CompilationResult<T> predecessorCompilationResult) {
        CompilationResult<T> compilationResult = getCompilationResult(identifierNode, predecessorCompilationResult);
        return getBuildSuccessorCompilationResultIfApplicable(identifierNode, compilationResult);
    }

    private CompilationResult<T> getBuildSuccessorCompilationResultIfApplicable(IdentifierNode identifierNode,
            CompilationResult<T> compilationResult) {
        if (identifierNode.hasSuccessor()) {
            IdentifierNode successorNode = identifierNode.getSuccessor();
            IdentifierNodeGenerator<T> successorBuilder = getGeneratorFor(successorNode);
            return successorBuilder.generateNode(successorNode, compilationResult);
        } else {
            return compilationResult;
        }
    }

    protected IdentifierNodeGenerator<T> getGeneratorFor(IdentifierNode node) {
        IdentifierNodeType nodeType = IdentifierNodeType.getNodeType(node.getClass());
        return nodeType.getGeneratorFor(getNodeGeneratorFactory());
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
            CompilationResult<T> contextCompilationResult);

    protected abstract CompilationResult<T> getErrorCompilationResult(InvalidIdentifierNode invalidIdentifierNode);

    private boolean isInvalidNode(IdentifierNode identifierNode) {
        return identifierNode.getClass() == IdentifierNodeType.INVALID_IDENTIFIER.getNodeClass();
    }

    protected IdentifierNodeGeneratorFactory<T> getNodeGeneratorFactory() {
        return nodeBuilderFactory;
    }
}
