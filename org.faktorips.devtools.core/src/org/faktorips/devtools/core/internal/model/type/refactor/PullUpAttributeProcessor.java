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

package org.faktorips.devtools.core.internal.model.type.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.refactor.IpsPullUpProcessor;

/**
 * Refactoring processor for the "Pull Up Attribute" - refactoring.
 * 
 * @since 3.4
 * 
 * @author Alexander Weickmann
 */
public class PullUpAttributeProcessor extends IpsPullUpProcessor {

    public PullUpAttributeProcessor(IAttribute attribute) {
        super(attribute);
    }

    @Override
    protected void addIpsSrcFiles() throws CoreException {
        addIpsSrcFile(getIpsSrcFile());
        IType supertype = getAttribute().getType().findSupertype(getIpsProject());
        addIpsSrcFile(supertype.getIpsSrcFile());
    }

    @Override
    protected void refactorIpsModel(IProgressMonitor pm) throws CoreException {
        pullUpAttribute();
    }

    private void pullUpAttribute() throws CoreException {
        IType superType = getAttribute().getType().findSupertype(getIpsProject());
        IAttribute newAttribute = superType.newAttribute();
        getAttribute().copy(newAttribute);
        getAttribute().delete();
    }

    @Override
    public RefactoringStatus validateUserInput(IProgressMonitor pm) throws CoreException {
        return new RefactoringStatus();
    }

    @Override
    public boolean isSourceFilesSavedRequired() {
        return false;
    }

    @Override
    public String getIdentifier() {
        return "org.faktorips.devtools.core.internal.model.type.refactor.PullUpAttributeProcessor"; //$NON-NLS-1$
    }

    @Override
    public String getProcessorName() {
        return Messages.PullUpAttributeProcessor_processorName;
    }

    private IIpsSrcFile getIpsSrcFile() {
        return getAttribute().getIpsSrcFile();
    }

    private IAttribute getAttribute() {
        return (IAttribute)getIpsElement();
    }

}
