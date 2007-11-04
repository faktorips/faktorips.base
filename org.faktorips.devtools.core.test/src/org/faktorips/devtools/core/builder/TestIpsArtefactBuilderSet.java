/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.devtools.core.internal.model.TableContentsEnumDatatypeAdapter;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.IdentifierResolver;

public class TestIpsArtefactBuilderSet extends AbstractBuilderSet {

    public final static String ID = "testbuilderset";
    
    private IIpsArtefactBuilder[] artefactBuilders;
    private boolean inverseRelationLinkRequiredFor2WayCompositions = false;
    private boolean roleNamePluralRequiredForTo1Relations = true;
    private boolean isAggregateRootBuilder = false;

    public TestIpsArtefactBuilderSet(){
        artefactBuilders = new IIpsArtefactBuilder[0];
    }

    public void setAggregateRootBuilder(boolean enable){
        isAggregateRootBuilder = enable;
    }
    
    public boolean containsAggregateRootBuilder() {
        return isAggregateRootBuilder;
    }

    public TestIpsArtefactBuilderSet(IIpsArtefactBuilder[] builders){
        artefactBuilders = builders;
    }
    
    public IIpsArtefactBuilder[] getArtefactBuilders() {
        return artefactBuilders;
    }
    
    public boolean isInverseRelationLinkRequiredFor2WayCompositions() {
        return inverseRelationLinkRequiredFor2WayCompositions;
    }

    public void setInverseRelationLinkRequiredFor2WayCompositions(boolean inverseRelationLinkRequiredFor2WayCompositions) {
        this.inverseRelationLinkRequiredFor2WayCompositions = inverseRelationLinkRequiredFor2WayCompositions;
    }

    public boolean isRoleNamePluralRequiredForTo1Relations() {
        return roleNamePluralRequiredForTo1Relations;
    }

    public void setRoleNamePluralRequiredForTo1Relations(boolean roleNamePluralRequiredForTo1Relations) {
        this.roleNamePluralRequiredForTo1Relations = roleNamePluralRequiredForTo1Relations;
    }

    public void setLogger(ILog logger) {
    }

    public void setId(String id) {
    }

    public void setLabel(String label) {
    }

    public String getId() {
        return ID;
    }

    public String getLabel() {
        return getId();
    }

    public boolean isSupportTableAccess() {
        return false;
    }

    public CompilationResult getTableAccessCode(ITableContents tableContents, ITableAccessFunction fct, CompilationResult[] argResults) throws CoreException {
        return null;
    }

	public boolean isSupportFlIdentifierResolver() {
		return false;
	}

	public IdentifierResolver createFlIdentifierResolver(IFormula formula) throws CoreException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPackage(String kind, IIpsSrcFile ipsSrcFile) throws CoreException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public IFile getRuntimeRepositoryTocFile(IIpsPackageFragmentRoot root) throws CoreException {
		return null;
	}

    /**
     * {@inheritDoc}
     */
    public String getClassNameForTableBasedEnum(ITableStructure structure) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getTocFilePackageName(IIpsPackageFragmentRoot root) throws CoreException {
        return null;
    }

    public void initialize(IIpsArtefactBuilderSetConfig config) throws CoreException {
    }

    /**
     * {@inheritDoc}
     */
    public String getRuntimeRepositoryTocResourceName(IIpsPackageFragmentRoot root) throws CoreException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public DatatypeHelper getDatatypeHelperForTableBasedEnum(TableContentsEnumDatatypeAdapter datatype) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getVersion() {
        return null;
    }
}
