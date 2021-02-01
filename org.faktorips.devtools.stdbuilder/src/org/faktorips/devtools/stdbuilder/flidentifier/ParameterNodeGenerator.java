/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.flidentifier;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.ParameterNode;
import org.faktorips.devtools.model.method.IParameter;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;

/**
 * JavaBuilder for a {@link ParameterNode}. Example in formula language: "policy" (use the function
 * argument policy).
 * 
 * @author frank
 * @since 3.11.0
 */
public class ParameterNodeGenerator extends StdBuilderIdentifierNodeGenerator {

    public ParameterNodeGenerator(IdentifierNodeGeneratorFactory<JavaCodeFragment> factory,
            StandardBuilderSet builderSet) {
        super(factory, builderSet);
    }

    @Override
    protected CompilationResult<JavaCodeFragment> getCompilationResultForCurrentNode(IdentifierNode identifierNode,
            CompilationResult<JavaCodeFragment> contextCompilationResult) {
        ParameterNode parameterNode = (ParameterNode)identifierNode;
        Datatype datatype = parameterNode.getDatatype();
        IParameter parameter = parameterNode.getParameter();
        return new CompilationResultImpl(parameter.getName(), datatype);
    }

}
