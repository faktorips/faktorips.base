/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.runtime.Message;

/**
 * Visitor that visits the parse tree and generates the {@link JavaCodeFragment Java source code}
 * that represents the expression in Java.
 */
class JavaParseTreeVisitor extends ParseTreeVisitor<JavaCodeFragment> {

    JavaParseTreeVisitor(JavaExprCompiler compiler) {
        super(compiler);
    }

    @Override
    protected CompilationResultImpl newCompilationResultImpl(String sourcecode, Datatype datatype) {
        return new CompilationResultImpl(sourcecode, datatype);
    }

    @Override
    protected CompilationResultImpl newCompilationResultImpl(JavaCodeFragment sourcecode, Datatype datatype) {
        return new CompilationResultImpl(sourcecode, datatype);
    }

    @Override
    protected CompilationResultImpl newCompilationResultImpl(Message message) {
        return new CompilationResultImpl(message);
    }

    @Override
    protected CompilationResultImpl newCompilationResultImpl() {
        return new CompilationResultImpl();
    }

}
