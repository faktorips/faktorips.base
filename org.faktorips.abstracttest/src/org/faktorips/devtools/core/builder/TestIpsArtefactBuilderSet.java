/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.builder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsArtefactBuilderSetConfig;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;

public class TestIpsArtefactBuilderSet extends AbstractBuilderSet {

    public final static String ID = "testbuilderset";

    private boolean inverseRelationLinkRequiredFor2WayCompositions = false;
    private boolean roleNamePluralRequiredForTo1Relations = true;
    private boolean isAggregateRootBuilder = false;

    public TestIpsArtefactBuilderSet() throws CoreException {
        this(new IIpsArtefactBuilder[0]);
    }

    public TestIpsArtefactBuilderSet(IIpsArtefactBuilder[] builders) throws CoreException {
        super(builders);
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(CONFIG_PROPERTY_GENERATOR_LOCALE, Locale.GERMAN.getLanguage());
        IpsArtefactBuilderSetConfig config = new IpsArtefactBuilderSetConfig(properties);
        initialize(config);
    }

    @Override
    protected IIpsArtefactBuilder[] createBuilders() throws CoreException {
        return getArtefactBuilders(); // are passed into the constructor
    }

    public void setAggregateRootBuilder(boolean enable) {
        isAggregateRootBuilder = enable;
    }

    @Override
    public boolean containsAggregateRootBuilder() {
        return isAggregateRootBuilder;
    }

    @Override
    public boolean isInverseRelationLinkRequiredFor2WayCompositions() {
        return inverseRelationLinkRequiredFor2WayCompositions;
    }

    public void setInverseRelationLinkRequiredFor2WayCompositions(boolean inverseRelationLinkRequiredFor2WayCompositions) {
        this.inverseRelationLinkRequiredFor2WayCompositions = inverseRelationLinkRequiredFor2WayCompositions;
    }

    @Override
    public boolean isRoleNamePluralRequiredForTo1Relations() {
        return roleNamePluralRequiredForTo1Relations;
    }

    public void setRoleNamePluralRequiredForTo1Relations(boolean roleNamePluralRequiredForTo1Relations) {
        this.roleNamePluralRequiredForTo1Relations = roleNamePluralRequiredForTo1Relations;
    }

    @Override
    public void setId(String id) {

    }

    @Override
    public void setLabel(String label) {

    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getLabel() {
        return getId();
    }

    @Override
    public boolean isSupportTableAccess() {
        return false;
    }

    @Override
    public CompilationResult getTableAccessCode(ITableContents tableContents,
            ITableAccessFunction fct,
            CompilationResult[] argResults) throws CoreException {
        return null;
    }

    @Override
    public boolean isSupportFlIdentifierResolver() {
        return false;
    }

    @Override
    public IdentifierResolver createFlIdentifierResolver(IFormula formula, ExprCompiler exprCompiler)
            throws CoreException {
        return null;
    }

    @Override
    public IdentifierResolver createFlIdentifierResolverForFormulaTest(IFormula formula, ExprCompiler exprCompiler)
            throws CoreException {
        return null;
    }

    @Override
    public String getPackage(String kind, IIpsSrcFile ipsSrcFile) throws CoreException {
        return null;
    }

    @Override
    public IFile getRuntimeRepositoryTocFile(IIpsPackageFragmentRoot root) throws CoreException {
        return null;
    }

    @Override
    public String getTocFilePackageName(IIpsPackageFragmentRoot root) throws CoreException {
        return null;
    }

    @Override
    public String getRuntimeRepositoryTocResourceName(IIpsPackageFragmentRoot root) throws CoreException {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public DatatypeHelper getDatatypeHelperForEnumType(EnumTypeDatatypeAdapter datatypeAdapter) {
        return null;
    }

}
