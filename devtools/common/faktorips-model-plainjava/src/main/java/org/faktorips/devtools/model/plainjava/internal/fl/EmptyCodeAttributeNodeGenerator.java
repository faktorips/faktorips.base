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
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.AttributeNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.fl.CompilationResult;

/**
 * JavaGenerator for an {@link AttributeNode}. Supports both policy- and product-attributes.
 * Examples in the formula language: "policy.premium" (gets the value of attribute "premium" from
 * policy) and "policy.paymentMode" (gets the value "paymentMode" from the configuring product
 * component).
 * 
 * @since 23.1
 */
public class EmptyCodeAttributeNodeGenerator extends EmptyCodeIdentifierNodeGenerator {

    public EmptyCodeAttributeNodeGenerator(IdentifierNodeGeneratorFactory<JavaCodeFragment> nodeBuilderFactory) {
        super(nodeBuilderFactory);
    }

    @Override
    protected CompilationResult<JavaCodeFragment> getCompilationResultForCurrentNode(IdentifierNode identifierNode,
            CompilationResult<JavaCodeFragment> contextCompilationResult) {
        if (isListOfTypeDatatype(contextCompilationResult) && !identifierNode.isListOfTypeDatatype()) {
            throw new IpsException("The datatype of this node is not a ListOfTypeDatatype: " + identifierNode); //$NON-NLS-1$
        }
        return super.getCompilationResultForCurrentNode(identifierNode, contextCompilationResult);
    }

    private boolean isListOfTypeDatatype(CompilationResult<JavaCodeFragment> compilationResult) {
        return compilationResult.getDatatype() instanceof ListOfTypeDatatype;
    }

}
