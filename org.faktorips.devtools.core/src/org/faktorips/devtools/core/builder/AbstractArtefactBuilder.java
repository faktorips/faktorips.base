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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.devtools.core.model.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.util.ArgumentCheck;

/**
 * Abstract base class for artefact builders.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractArtefactBuilder implements IIpsArtefactBuilder {

	private IIpsArtefactBuilderSet builderSet;
	
	public AbstractArtefactBuilder(IIpsArtefactBuilderSet builderSet) {
		ArgumentCheck.notNull(builderSet);
		this.builderSet = builderSet;
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsArtefactBuilderSet getBuilderSet() {
		return builderSet;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void beforeBuildProcess(IIpsProject project, int buildKind) throws CoreException {
		// default implementation does nothing
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterBuildProcess(IIpsProject project, int buildKind) throws CoreException {
		// default implementation does nothing
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException {
		// default implementation does nothing
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterBuild(IIpsSrcFile ipsSrcFile) throws CoreException {
		// default implementation does nothing
	}

    /**
     * Returns false.
     */
	public boolean buildsDerivedArtefacts() {
        return false;
    }

    /**
	 * {@inheritDoc}
	 */
	public String toString() {
		return getName();
	}

}
