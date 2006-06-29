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
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IParameterIdentifierResolver;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;

/**
 * An IIpsArtefactBuilderSet implementation that is supposed to be used in cases
 * where no builder set has been registered for a specific IIpsProject. It
 * returns an empty IIpsArtefactBuilder array and supports formula language
 * capabilities in a way that the fl-compiler can check against the model but
 * the compiled code is no valid java code.
 * 
 * @author Peter Erzberger
 */
public class EmptyBuilderSet extends AbstractBuilderSet {

	/**
	 * {@inheritDoc}
	 */
	public IIpsArtefactBuilder[] getArtefactBuilders() {
		return new IIpsArtefactBuilder[0];
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSupportTableAccess() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSupportFlIdentifierResolver() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public CompilationResult getTableAccessCode(ITableContents tableContents, ITableAccessFunction fct,
			CompilationResult[] argResults) throws CoreException {
        Datatype returnType = fct.getIpsProject().findDatatype(fct.getType());
        JavaCodeFragment code = new JavaCodeFragment();
        return new CompilationResultImpl(code, returnType);
	}

	/**
	 * {@inheritDoc}
	 */
	public IParameterIdentifierResolver getFlParameterIdentifierResolver() {
		return new AbstractParameterIdentifierResolver() {

			protected String getParameterAttributGetterName(
					IAttribute attribute, Datatype datatype) {
				return ""; //$NON-NLS-1$
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	public void initialize() throws CoreException {
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
	public void clean() throws CoreException {
		// do nothing
	}
}
