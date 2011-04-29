/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.type.IAttribute;
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
    public CompilationResult getTableAccessCode(ITableContents tableContents,
            ITableAccessFunction fct,
            CompilationResult[] argResults) throws CoreException {

        Datatype returnType = fct.getIpsProject().findDatatype(fct.getType());
        JavaCodeFragment code = new JavaCodeFragment();
        return new CompilationResultImpl(code, returnType);
    }

    @Override
    public IdentifierResolver createFlIdentifierResolver(IFormula formula, ExprCompiler exprCompiler)
            throws CoreException {

        return new AbstractParameterIdentifierResolver(formula, exprCompiler) {
            @Override
            protected String getParameterAttributGetterName(IAttribute attribute, Datatype datatype) {
                return ""; //$NON-NLS-1$
            }
        };
    }

    @Override
    public IdentifierResolver createFlIdentifierResolverForFormulaTest(IFormula formula, ExprCompiler exprCompiler)
            throws CoreException {

        return createFlIdentifierResolver(formula, exprCompiler);
    }

    @Override
    public IFile getRuntimeRepositoryTocFile(IIpsPackageFragmentRoot root) throws CoreException {
        return null;
    }

    @Override
    public String getRuntimeRepositoryTocResourceName(IIpsPackageFragmentRoot root) throws CoreException {
        return null;
    }

    @Override
    public String getTocFilePackageName(IIpsPackageFragmentRoot root) throws CoreException {
        return null;
    }

    /**
     * Returns the string <i>emptyBuilderSet</i>
     */
    @Override
    public String getId() {
        return "emptyBuilderSet"; //$NON-NLS-1$
    }

    /**
     * Calls to this method are ignored. The getId() method always returns <i>emptyBuilderSet</i>
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
    protected IIpsArtefactBuilder[] createBuilders() throws CoreException {
        return new IIpsArtefactBuilder[0];
    }

    @Override
    public DatatypeHelper getDatatypeHelperForEnumType(EnumTypeDatatypeAdapter datatypeAdapter) {
        return null;
    }

}
