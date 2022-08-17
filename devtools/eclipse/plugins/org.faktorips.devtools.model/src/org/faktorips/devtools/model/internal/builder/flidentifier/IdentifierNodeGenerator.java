/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.builder.flidentifier;

import org.faktorips.codegen.CodeFragment;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNodeType;
import org.faktorips.fl.CompilationResult;

/**
 * Base class for all generators that create code for a given {@link IdentifierNode}. This class
 * defines the basic process through which a node and it's successors are being processed.
 * <p>
 * Subclasses implement the
 * {@link #getCompilationResultForCurrentNode(IdentifierNode, CompilationResult)} method to provide
 * the {@link CompilationResult} for a single type of node.
 * 
 * @author widmaier
 */
public abstract class IdentifierNodeGenerator<T extends CodeFragment> {

    public static final String DEFAULT_VALUE_SUFFIX = "@default"; //$NON-NLS-1$
    private final IdentifierNodeGeneratorFactory<T> nodeBuilderFactory;

    public IdentifierNodeGenerator(IdentifierNodeGeneratorFactory<T> factory) {
        this.nodeBuilderFactory = factory;
    }

    /**
     * Creates a {@link CompilationResult} containing the source code for the given
     * {@link IdentifierNode node}.
     * <p>
     * This method also generates the {@link CompilationResult} of the given node's successor
     * (following identifier part), if there is one, by delegating to its
     * {@link #generateNode(IdentifierNode, CompilationResult)} method (recursively). The resulting
     * {@link CompilationResult} thus contains either the complete code for the given node an its
     * successors or error messages.
     * 
     * @param identifierNode the node to process
     * @param contextCompilationResult the {@link CompilationResult} of the predecessor node. The
     *            context code is usually a statement this builder appends its node's code to (as in
     *            "contextCode.someFunction()") or code that is used as an argument for a function
     *            this builder generates (as in "someFunction(contextCode)").
     * @return The {@link CompilationResult} containing the complete code for the given identifier
     *             and all of its successors.
     * 
     * @see #getCompilationResultForCurrentNode(IdentifierNode, CompilationResult)
     */
    public CompilationResult<T> generateNode(IdentifierNode identifierNode,
            CompilationResult<T> contextCompilationResult) {
        return generateNodeAndSuccessors(identifierNode, contextCompilationResult);
    }

    private CompilationResult<T> generateNodeAndSuccessors(IdentifierNode identifierNode,
            CompilationResult<T> predecessorCompilationResult) {
        CompilationResult<T> compilationResult = getCompilationResultForCurrentNode(identifierNode,
                predecessorCompilationResult);
        return getSuccessorCompilationResultIfApplicable(identifierNode, compilationResult);
    }

    private CompilationResult<T> getSuccessorCompilationResultIfApplicable(IdentifierNode identifierNode,
            CompilationResult<T> compilationResult) {
        if (compilationResult != null && !compilationResult.failed() && identifierNode.hasSuccessor()) {
            return getSuccessorCompilationResult(identifierNode, compilationResult);
        } else {
            return compilationResult;
        }
    }

    private CompilationResult<T> getSuccessorCompilationResult(IdentifierNode identifierNode,
            CompilationResult<T> compilationResult) {
        IdentifierNode successorNode = identifierNode.getSuccessor();
        IdentifierNodeGenerator<T> successorBuilder = getGeneratorFor(successorNode);
        return successorBuilder.generateNode(successorNode, compilationResult);
    }

    /**
     * Returns the {@link IdentifierNodeGenerator} for the given node.
     * 
     * @param node the node to create a builder for.
     */
    protected IdentifierNodeGenerator<T> getGeneratorFor(IdentifierNode node) {
        IdentifierNodeType nodeType = IdentifierNodeType.getNodeType(node.getClass());
        return nodeType.getGenerator(getNodeGeneratorFactory());
    }

    /**
     * Generates the compilation result for the given node only.
     * 
     * @param identifierNode the node this builder is currently processing. This node is always a
     *            "valid" node, never an InvalidNode.
     * @param contextCompilationResult The {@link CompilationResult} for the predecessor of the
     *            given {@link IdentifierNode}. The context code is usually a statement this builder
     *            appends its node's code to (as in "contextCode.someFunction()") or code that is
     *            used as an argument for a function this builder generates (as in
     *            "someFunction(contextCode)").
     * @return a {@link CompilationResult} containing the code this builder generates for the given
     *             {@link IdentifierNode}.
     */
    protected abstract CompilationResult<T> getCompilationResultForCurrentNode(IdentifierNode identifierNode,
            CompilationResult<T> contextCompilationResult);

    /**
     * Returns the {@link IdentifierNodeGeneratorFactory} this generator was created with.
     */
    protected IdentifierNodeGeneratorFactory<T> getNodeGeneratorFactory() {
        return nodeBuilderFactory;
    }
}
