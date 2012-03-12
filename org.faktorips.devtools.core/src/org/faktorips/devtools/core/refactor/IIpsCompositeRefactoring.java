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

package org.faktorips.devtools.core.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Allows batch execution of multiple {@link IIpsRefactoring}s.
 * 
 * @author Alexander Weickmann
 */
public interface IIpsCompositeRefactoring extends IIpsRefactoring {

    /**
     * Returns of how many refactorings this composite refactoring consists of.
     */
    public int getNumberOfRefactorings();

    /**
     * {@inheritDoc}
     * <p>
     * In a composite refactoring source files must be saved if one or more contained refactorings
     * require it.
     */
    @Override
    public boolean isSourceFilesSavedRequired();

    /**
     * {@inheritDoc}
     * <p>
     * In a composite refactoring the result of the initial condition checking is the aggregate
     * result of checking the initial conditions of each contained refactoring.
     */
    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException,
            OperationCanceledException;

    /**
     * {@inheritDoc}
     * <p>
     * In a composite refactoring the result of the final condition checking is the aggregate result
     * of checking the final conditions of each contained refactoring. The LTK refactoring
     * architecture requires that the final conditions are checked only once however. Therefore and
     * due to technical implementation details the final conditions of a composite refactoring
     * cannot be computed. Instead the final conditions of each contained refactoring are checked
     * right before the refactoring is executed. For more details see
     * {@link #createChange(IProgressMonitor)}.
     * <p>
     * This implementation always returns an empty {@link RefactoringStatus}.
     */
    @Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException;

    /**
     * {@inheritDoc}
     * <p>
     * In a composite refactoring the contained refactorings will be executed successively. Thereby
     * the final conditions of each refactoring are checked before the refactoring is executed. The
     * result of the condition check is logged using an appropriate {@link IStatus}. Should the
     * outcome of the condition check for a given refactoring is {@link RefactoringStatus#FATAL}
     * this specific refactoring will not be executed.
     * <p>
     * Always returns a {@link NullChange}.
     */
    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException;

    /**
     * {@inheritDoc}
     * <p>
     * Composite refactorings can be canceled if they are composed of more than 1 child refactoring.
     * On cancellation the currently processed refactoring will be completed and the remaining
     * refactorings will not be started.
     */
    @Override
    public boolean isCancelable();

}
