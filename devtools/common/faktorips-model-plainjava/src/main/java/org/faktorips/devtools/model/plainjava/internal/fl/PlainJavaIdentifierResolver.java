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
import org.faktorips.devtools.model.internal.builder.flidentifier.AbstractIdentifierResolver;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;

public class PlainJavaIdentifierResolver extends AbstractIdentifierResolver<JavaCodeFragment> {

    public PlainJavaIdentifierResolver(IExpression expression, ExprCompiler<JavaCodeFragment> exprCompiler) {
        super(expression, exprCompiler);
    }

    @Override
    protected IdentifierNodeGeneratorFactory<JavaCodeFragment> getGeneratorFactory() {
        return new EmptyCodeIdentifierNodeGeneratorFactory();
    }

    @Override
    protected CompilationResult<JavaCodeFragment> getStartingCompilationResult() {
        return new CompilationResultImpl("this", getExpression().findProductCmptType(getIpsProject()));
    }

}