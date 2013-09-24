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

package org.faktorips.devtools.stdbuilder;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.builder.ExtendedExprCompiler;
import org.faktorips.devtools.core.builder.flidentifier.AbstractIdentifierResolver;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeGenerator;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.stdbuilder.flidentifier.AssociationNodeGenerator;
import org.faktorips.devtools.stdbuilder.flidentifier.AttributeNodeGenerator;
import org.faktorips.devtools.stdbuilder.flidentifier.EnumNodeGenerator;
import org.faktorips.devtools.stdbuilder.flidentifier.IndexBasedAssociationNodeGenerator;
import org.faktorips.devtools.stdbuilder.flidentifier.InvalidNodeGenerator;
import org.faktorips.devtools.stdbuilder.flidentifier.ParameterNodeGenerator;
import org.faktorips.devtools.stdbuilder.flidentifier.QualifiedAssociationNodeGenerator;
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
        return new StdIdentifierNodeGeneratorFactory(builderSet, getExprCompiler());
    }

    @Override
    protected CompilationResult<JavaCodeFragment> getStartingCompilationResult() {
        return new CompilationResultImpl("this", getExpression().findProductCmptType(getIpsproject()));
    }

    private static class StdIdentifierNodeGeneratorFactory implements IdentifierNodeGeneratorFactory<JavaCodeFragment> {

        private final StandardBuilderSet builderSet;
        private final ExtendedExprCompiler exprCompiler;

        public StdIdentifierNodeGeneratorFactory(StandardBuilderSet builderSet, ExtendedExprCompiler exprCompiler) {
            this.builderSet = builderSet;
            this.exprCompiler = exprCompiler;
        }

        @Override
        public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForAssociationNode() {
            return new AssociationNodeGenerator(this, builderSet);
        }

        @Override
        public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForAttributeNode() {
            return new AttributeNodeGenerator(this, builderSet);
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
            return new IdentifierNodeGenerator<JavaCodeFragment>(this) {

                @Override
                protected CompilationResult<JavaCodeFragment> getCompilationResultForCurrentNode(IdentifierNode identifierNode,
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
            return new IndexBasedAssociationNodeGenerator(this, builderSet);
        }

        @Override
        public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForParameterNode() {
            return new ParameterNodeGenerator(this, builderSet);
        }

        @Override
        public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForQualifiedAssociationNode() {
            return new QualifiedAssociationNodeGenerator(this, builderSet);
        }

        @Override
        public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForInvalidNode() {
            return new InvalidNodeGenerator(this, builderSet);
        }
    }

}
