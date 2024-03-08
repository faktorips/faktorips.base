/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.fl.identifier;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.builder.java.JavaBuilderSet;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierParser;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;

/**
 * JavaGenerator for a {@link InvalidIdentifierNode}. Returns in the {@link CompilationResult} the
 * error message from the {@link IdentifierParser}.
 * 
 * @author dirmaier
 * @since 3.11.0
 */
public class InvalidNodeGenerator extends JavaBuilderIdentifierNodeGenerator {

    public InvalidNodeGenerator(IdentifierNodeGeneratorFactory<JavaCodeFragment> factory,
            JavaBuilderSet builderSet) {
        super(factory, builderSet);
    }

    @Override
    protected CompilationResult<JavaCodeFragment> getCompilationResultForCurrentNode(IdentifierNode identifierNode,
            CompilationResult<JavaCodeFragment> contextCompilationResult) {
        InvalidIdentifierNode invalidIdentifierNode = (InvalidIdentifierNode)identifierNode;
        return new CompilationResultImpl(invalidIdentifierNode.getMessage());
    }

}
