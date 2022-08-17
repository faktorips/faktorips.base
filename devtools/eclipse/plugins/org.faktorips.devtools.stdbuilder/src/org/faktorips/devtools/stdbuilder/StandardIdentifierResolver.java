/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.builder.ExtendedExprCompiler;
import org.faktorips.devtools.model.internal.builder.flidentifier.AbstractIdentifierResolver;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGenerator;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.stdbuilder.flidentifier.AssociationNodeGenerator;
import org.faktorips.devtools.stdbuilder.flidentifier.AttributeNodeGenerator;
import org.faktorips.devtools.stdbuilder.flidentifier.EnumNodeGenerator;
import org.faktorips.devtools.stdbuilder.flidentifier.IndexNodeGenerator;
import org.faktorips.devtools.stdbuilder.flidentifier.InvalidNodeGenerator;
import org.faktorips.devtools.stdbuilder.flidentifier.ParameterNodeGenerator;
import org.faktorips.devtools.stdbuilder.flidentifier.QualifierNodeGenerator;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;

public class StandardIdentifierResolver extends AbstractIdentifierResolver<JavaCodeFragment> {

    private final StandardBuilderSet builderSet;

    public StandardIdentifierResolver(IExpression expression, ExtendedExprCompiler exprCompiler,
            StandardBuilderSet builderSet) {
        super(expression, exprCompiler);
        this.builderSet = builderSet;
    }

    @Override
    public ExtendedExprCompiler getExprCompiler() {
        return (ExtendedExprCompiler)super.getExprCompiler();
    }

    @Override
    protected IdentifierNodeGeneratorFactory<JavaCodeFragment> getGeneratorFactory() {
        return new StdIdentifierNodeGeneratorFactory(builderSet, getExpression(), getExprCompiler());
    }

    @Override
    protected CompilationResult<JavaCodeFragment> getStartingCompilationResult() {
        return new CompilationResultImpl("this", getExpression().findProductCmptType(getIpsProject())); //$NON-NLS-1$
    }

    private static class StdIdentifierNodeGeneratorFactory implements IdentifierNodeGeneratorFactory<JavaCodeFragment> {

        private final StandardBuilderSet builderSet;

        private final ExtendedExprCompiler exprCompiler;

        private final IExpression expression;

        public StdIdentifierNodeGeneratorFactory(StandardBuilderSet builderSet, IExpression expression,
                ExtendedExprCompiler exprCompiler) {
            this.builderSet = builderSet;
            this.expression = expression;
            this.exprCompiler = exprCompiler;
        }

        @Override
        public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForParameterNode() {
            return new ParameterNodeGenerator(this, builderSet);
        }

        @Override
        public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForAssociationNode() {
            return new AssociationNodeGenerator(this, builderSet);
        }

        @Override
        public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForAttributeNode() {
            return new AttributeNodeGenerator(this, expression, builderSet);
        }

        /**
         * {@inheritDoc}
         * 
         * This implementation only returns an empty generator that returns the incoming context
         * result. The real code generation for enum values is done by the {@link EnumNodeGenerator}
         * returned by {@link #getGeneratorForEnumValueNode()}.
         */
        @Override
        public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForEnumClassNode() {
            return new IdentifierNodeGenerator<>(this) {

                @Override
                protected CompilationResult<JavaCodeFragment> getCompilationResultForCurrentNode(
                        IdentifierNode identifierNode,
                        CompilationResult<JavaCodeFragment> contextCompilationResult) {
                    return contextCompilationResult;
                }

            };
        }

        @Override
        public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForEnumValueNode() {
            return new EnumNodeGenerator(this, builderSet, exprCompiler);
        }

        @Override
        public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForIndexBasedAssociationNode() {
            return new IndexNodeGenerator(this, builderSet);
        }

        @Override
        public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForQualifiedAssociationNode() {
            return new QualifierNodeGenerator(this, builderSet);
        }

        @Override
        public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForInvalidNode() {
            return new InvalidNodeGenerator(this, builderSet);
        }
    }

}
