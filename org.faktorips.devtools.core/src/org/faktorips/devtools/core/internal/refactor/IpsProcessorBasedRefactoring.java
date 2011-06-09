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

package org.faktorips.devtools.core.internal.refactor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.refactor.IIpsProcessorBasedRefactoring;
import org.faktorips.devtools.core.refactor.IpsRefactoringProcessor;

/**
 * @author Alexander Weickmann
 */
public final class IpsProcessorBasedRefactoring extends ProcessorBasedRefactoring implements
        IIpsProcessorBasedRefactoring {

    public IpsProcessorBasedRefactoring(IpsRefactoringProcessor ipsRefactoringProcessor) {
        super(ipsRefactoringProcessor);
    }

    @Override
    public RefactoringStatus validateUserInput(IProgressMonitor pm) throws CoreException {
        return getIpsRefactoringProcessor().validateUserInput(pm);
    }

    @Override
    public boolean isSourceFilesSavedRequired() {
        return getIpsRefactoringProcessor().isSourceFilesSavedRequired();
    }

    @Override
    public IpsRefactoringProcessor getIpsRefactoringProcessor() {
        return (IpsRefactoringProcessor)getProcessor();
    }

    @Override
    public ProcessorBasedRefactoring toLtkRefactoring() {
        return this;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }

    @Override
    public IIpsProject getIpsProject() {
        return getIpsRefactoringProcessor().getIpsElement().getIpsProject();
    }

    @Override
    public Set<IIpsElement> getIpsElements() {
        HashSet<IIpsElement> elements = new HashSet<IIpsElement>(1);
        elements.add(getIpsRefactoringProcessor().getIpsElement());
        return elements;
    }

}
