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

import java.util.Locale;

import org.faktorips.codegen.CodeFragment;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;
import org.faktorips.util.ArgumentCheck;

public abstract class AbstractIdentifierResolver<T extends CodeFragment> implements IdentifierResolver<T> {

    private final IExpression expression;
    private final ExprCompiler<JavaCodeFragment> exprCompiler;
    private final IIpsProject ipsProject;
    private final IdentifierParser parser;

    public AbstractIdentifierResolver(IExpression expression, ExprCompiler<JavaCodeFragment> exprCompiler) {
        ArgumentCheck.notNull(expression, this);
        ArgumentCheck.notNull(exprCompiler, this);
        this.expression = expression;
        this.exprCompiler = exprCompiler;
        ipsProject = expression.getIpsProject();
        this.parser = new IdentifierParser(expression, ipsProject);
    }

    public IExpression getExpression() {
        return expression;
    }

    public ExprCompiler<JavaCodeFragment> getExprCompiler() {
        return exprCompiler;
    }

    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    public IdentifierParser getParser() {
        return parser;
    }

    @Override
    public CompilationResult<T> compile(String identifier, ExprCompiler<T> exprCompiler, Locale locale) {
        IdentifierNode identifierNode = parseIdentifier(identifier);
        IdentifierNodeType nodeType = IdentifierNodeType.getNodeType(identifierNode.getClass());
        IdentifierNodeGenerator<T> generator = nodeType.getGenerator(getGeneratorFactory());
        CompilationResult<T> contextCompilationResult = getStartingCompilationResult();
        return generator.generateNode(identifierNode, contextCompilationResult);
    }

    public IdentifierNode parseIdentifier(String identifier) {
        IdentifierNode identifierNode = getParser().parse(identifier);
        return identifierNode;
    }

    protected abstract IdentifierNodeGeneratorFactory<T> getGeneratorFactory();

    protected abstract CompilationResult<T> getStartingCompilationResult();

}
