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

import java.util.Locale;

import org.faktorips.codegen.CodeFragment;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNodeType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;
import org.faktorips.util.ArgumentCheck;

public abstract class AbstractIdentifierResolver<T extends CodeFragment> implements IdentifierResolver<T> {

    private final IExpression expression;
    private final ExprCompiler<T> exprCompiler;
    private final IIpsProject ipsProject;
    private final IdentifierParser parser;

    public AbstractIdentifierResolver(IExpression expression, ExprCompiler<T> exprCompiler) {
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

    public ExprCompiler<T> getExprCompiler() {
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
        return getParser().parse(identifier);
    }

    protected abstract IdentifierNodeGeneratorFactory<T> getGeneratorFactory();

    protected abstract CompilationResult<T> getStartingCompilationResult();

}
