/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.builder;

import java.util.LinkedHashMap;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.builder.AbstractBuilderSet;
import org.faktorips.devtools.model.builder.IPersistenceProvider;
import org.faktorips.devtools.model.internal.builder.flidentifier.AbstractIdentifierResolver;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.model.ipsproject.IBuilderKindId;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.tablestructure.ITableAccessFunction;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;

/**
 * An IIpsArtefactBuilderSet implementation that is supposed to be used in cases where no builder
 * set has been registered for a specific IIpsProject. It returns an empty IIpsArtefactBuilder array
 * and supports formula language capabilities in a way that the fl-compiler can check against the
 * model but the compiled code is no valid java code.
 * 
 * @author Peter Erzberger
 */
public class EmptyBuilderSet extends AbstractBuilderSet {

    @Override
    public boolean isSupportTableAccess() {
        return true;
    }

    @Override
    public boolean isSupportFlIdentifierResolver() {
        return true;
    }

    @Override
    public CompilationResult<JavaCodeFragment> getTableAccessCode(String tableContentsQualifiedName,
            ITableAccessFunction fct,
            CompilationResult<JavaCodeFragment>[] argResults) {

        Datatype returnType = fct.getIpsProject().findDatatype(fct.getType());
        JavaCodeFragment code = new JavaCodeFragment();
        return new CompilationResultImpl(code, returnType);
    }

    @Override
    public IdentifierResolver<JavaCodeFragment> createFlIdentifierResolver(IExpression formula,
            ExprCompiler<JavaCodeFragment> exprCompiler) {
        return new EmptyParameterIdentifierResolver(formula, exprCompiler);
    }

    @Override
    public AFile getRuntimeRepositoryTocFile(IIpsPackageFragmentRoot root) {
        return null;
    }

    @Override
    public String getRuntimeRepositoryTocResourceName(IIpsPackageFragmentRoot root) {
        return null;
    }

    /**
     * Returns the string <em>emptyBuilderSet</em>
     */
    @Override
    public String getId() {
        return "emptyBuilderSet"; //$NON-NLS-1$
    }

    /**
     * Calls to this method are ignored. The getId() method always returns <em>emptyBuilderSet</em>
     */
    @Override
    public void setId(String id) {
        // Ignored.
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    protected LinkedHashMap<IBuilderKindId, IIpsArtefactBuilder> createBuilders() {
        return new LinkedHashMap<>();
    }

    @Override
    public IPersistenceProvider getPersistenceProvider() {
        return null;
    }

    @Override
    public DatatypeHelper getDatatypeHelper(Datatype datatype) {
        return null;
    }

    @Override
    public boolean usesUnifiedValueSets() {
        return false;
    }

    private static final class EmptyParameterIdentifierResolver extends AbstractIdentifierResolver<JavaCodeFragment> {

        public EmptyParameterIdentifierResolver(IExpression expression, ExprCompiler<JavaCodeFragment> exprCompiler) {
            super(expression, exprCompiler);
        }

        @Override
        protected IdentifierNodeGeneratorFactory<JavaCodeFragment> getGeneratorFactory() {
            return null;
        }

        @Override
        protected CompilationResult<JavaCodeFragment> getStartingCompilationResult() {
            return new CompilationResultImpl();
        }
    }

}
