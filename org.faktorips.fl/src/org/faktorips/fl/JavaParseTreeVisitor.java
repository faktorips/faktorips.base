/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.fl;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.parser.SimpleNode;
import org.faktorips.util.message.Message;

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

    @Override
    protected CompilationResultImpl generateConstant(SimpleNode node, DatatypeHelper helper) {
        String value = node.getLastToken().toString();
        return new CompilationResultImpl(helper.newInstance(value), helper.getDatatype());
    }

}
