/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.refactor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.faktorips.devtools.core.refactor.IIpsProcessorBasedRefactoring;
import org.faktorips.devtools.core.refactor.IpsRefactoringProcessor;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * @author Alexander Weickmann
 */
public final class IpsProcessorBasedRefactoring extends ProcessorBasedRefactoring implements
        IIpsProcessorBasedRefactoring {

    public IpsProcessorBasedRefactoring(IpsRefactoringProcessor ipsRefactoringProcessor) {
        super(ipsRefactoringProcessor);
    }

    @Override
    public RefactoringStatus validateUserInput(IProgressMonitor pm) throws CoreRuntimeException {
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
        HashSet<IIpsElement> elements = new HashSet<>(1);
        elements.add(getIpsRefactoringProcessor().getIpsElement());
        return elements;
    }

}
