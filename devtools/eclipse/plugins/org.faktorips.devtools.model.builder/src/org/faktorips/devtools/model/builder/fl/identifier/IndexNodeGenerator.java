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
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IndexNode;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;

/**
 * Generator for {@link IndexNode IndexBasedAssociationNodes}. Example in formula language:
 * "policy.converage[0]" (get the first coverage from policy).
 * 
 * @author frank
 * @since 3.11.0
 */
public class IndexNodeGenerator extends JavaBuilderIdentifierNodeGenerator {

    public IndexNodeGenerator(IdentifierNodeGeneratorFactory<JavaCodeFragment> factory, JavaBuilderSet builderSet) {
        super(factory, builderSet);
    }

    @Override
    protected CompilationResult<JavaCodeFragment> getCompilationResultForCurrentNode(IdentifierNode identifierNode,
            CompilationResult<JavaCodeFragment> contextCompilationResult) {
        IndexNode node = (IndexNode)identifierNode;
        JavaCodeFragment result = new JavaCodeFragment(contextCompilationResult.getCodeFragment());
        result.append(".get(").append(node.getIndex()).append(")");
        return new CompilationResultImpl(result, node.getDatatype());
    }
}
