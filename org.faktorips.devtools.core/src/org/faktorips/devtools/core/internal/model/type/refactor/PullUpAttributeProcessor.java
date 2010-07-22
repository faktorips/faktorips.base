/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.type.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.faktorips.devtools.core.internal.refactor.IpsRefactoringProcessor;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.refactor.PullUpArguments;

public class PullUpAttributeProcessor extends IpsRefactoringProcessor {

    public PullUpAttributeProcessor(IIpsElement ipsElement) {
        super(ipsElement);
    }

    @Override
    protected void addIpsSrcFiles() throws CoreException {
        // TODO AW: Implement method for pull up attribute.
    }

    @Override
    protected void refactorIpsModel(IProgressMonitor pm) throws CoreException {
        // TODO AW: Implement method for pull up attribute.
    }

    @Override
    public String getIdentifier() {
        return "org.faktorips.devtools.core.internal.model.type.refactor.PullUpAttributeProcessor"; //$NON-NLS-1$
    }

    @Override
    public String getProcessorName() {
        return "Pull Up Attribute Processor"; //$NON-NLS-1$
    }

    @Override
    public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants sharedParticipants)
            throws CoreException {

        RefactoringParticipant[] participants = new RefactoringParticipant[1];
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        // TODO AW: Move the following identifier strings to some interface.
        IConfigurationElement[] elements = registry
                .getConfigurationElementsFor("org.faktorips.devtools.core.pullUpParticipants"); //$NON-NLS-1$
        participants[0] = (RefactoringParticipant)elements[0].createExecutableExtension("class"); //$NON-NLS-1$
        participants[0].initialize(this, getIpsElement(), new PullUpArguments(true));
        return participants;
    }

    @Override
    public RefactoringStatus validateUserInput(IProgressMonitor pm) throws CoreException {
        return new RefactoringStatus();
    }

}
