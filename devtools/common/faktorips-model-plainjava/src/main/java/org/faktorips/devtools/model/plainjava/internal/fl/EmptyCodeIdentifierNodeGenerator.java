/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.plainjava.internal.fl;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGenerator;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;

/**
 * An {@link IdentifierNodeGenerator} that generates no source code but returns a
 * {@link CompilationResult} with the matching {@link Datatype}.
 */
class EmptyCodeIdentifierNodeGenerator extends IdentifierNodeGenerator<JavaCodeFragment> {

    public EmptyCodeIdentifierNodeGenerator(IdentifierNodeGeneratorFactory<JavaCodeFragment> factory) {
        super(factory);
    }

    @Override
    protected CompilationResult<JavaCodeFragment> getCompilationResultForCurrentNode(IdentifierNode identifierNode,
            CompilationResult<JavaCodeFragment> contextCompilationResult) {
        return new CompilationResultImpl(new JavaCodeFragment(), identifierNode.getDatatype());
    }
}