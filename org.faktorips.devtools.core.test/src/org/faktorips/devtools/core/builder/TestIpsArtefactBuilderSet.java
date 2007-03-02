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
import org.faktorips.devtools.core.internal.model.TableStructureEnumDatatypeAdapter;
import org.faktorips.devtools.core.model.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IParameterIdentifierResolver;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.IdentifierResolver;

public class TestIpsArtefactBuilderSet extends AbstractBuilderSet {

    public final static String ID = "testbuilderset";
    
    private IIpsArtefactBuilder[] artefactBuilders;
    
    public TestIpsArtefactBuilderSet(){
        artefactBuilders = new IIpsArtefactBuilder[0];
    }

    public TestIpsArtefactBuilderSet(IIpsArtefactBuilder[] builders){
        artefactBuilders = builders;
    }
    
    public IIpsArtefactBuilder[] getArtefactBuilders() {
        return artefactBuilders;
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

	public IdentifierResolver getFlIdentifierResolver() {
		return null;
	}

	public boolean isSupportFlIdentifierResolver() {
		return false;
	}

	public IParameterIdentifierResolver getFlParameterIdentifierResolver() {
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
    public DatatypeHelper getDatatypeHelperForTableBasedEnum(TableStructureEnumDatatypeAdapter datatype) {
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
}
