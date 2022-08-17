/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.refactor;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Refactoring interface to be implemented by all Faktor-IPS refactorings.
 * 
 * @author Alexander Weickmann
 */
public interface IIpsRefactoring {

    /**
     * Returns a copy of the set of IPS elements refactored by this IPS refactoring.
     */
    Set<IIpsElement> getIpsElements();

    /**
     * Validates any user input.
     * <p>
     * The contract of this method specifies that it may be called multiple times but has to be
     * called at least once before executing the refactoring.
     * 
     * @param pm An {@link IProgressMonitor} to report progress to
     * 
     * @throws IpsException If an error occurs while validating the user input
     */
    RefactoringStatus validateUserInput(IProgressMonitor pm) throws IpsException;

    /**
     * Returns whether this refactoring requires that all IPS source files are saved before the
     * refactoring may happen.
     */
    boolean isSourceFilesSavedRequired();

    /**
     * Returns whether the refactoring can be canceled by the user.
     */
    boolean isCancelable();

    /**
     * Allows to treat the IPS refactoring as LTK refactoring.
     */
    Refactoring toLtkRefactoring();

    /**
     * Returns the {@link IIpsProject} from which all the elements that are to be refactored
     * originate.
     */
    IIpsProject getIpsProject();

    /**
     * Method from {@link Refactoring}.
     * 
     * @see Refactoring#getName()
     */
    String getName();

    /**
     * Method from {@link Refactoring}.
     * 
     * @see Refactoring#checkInitialConditions(IProgressMonitor)
     */
    RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException,
            OperationCanceledException;

    /**
     * Method from {@link Refactoring}.
     * 
     * @see Refactoring#checkFinalConditions(IProgressMonitor)
     */
    RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException;

    /**
     * Method from {@link Refactoring}.
     * 
     * @see Refactoring#createChange(IProgressMonitor)
     */
    Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException;

}
