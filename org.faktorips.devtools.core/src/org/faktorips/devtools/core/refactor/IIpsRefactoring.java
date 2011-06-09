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

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Refactoring interface to be implemented by all Faktor-IPS refactorings.
 * 
 * @author Alexander Weickmann
 */
public interface IIpsRefactoring {

    /**
     * Returns a copy of the set of IPS elements refactored by this IPS refactoring.
     */
    public Set<IIpsElement> getIpsElements();

    /**
     * Validates any user input.
     * <p>
     * The contract of this method specifies that it may be called multiple times but has to be
     * called at least once before executing the refactoring.
     * 
     * @param pm An {@link IProgressMonitor} to report progress to
     * 
     * @throws CoreException If an error occurs while validating the user input
     */
    public RefactoringStatus validateUserInput(IProgressMonitor pm) throws CoreException;

    /**
     * Returns whether this refactoring requires that all IPS source files are saved before the
     * refactoring may happen.
     */
    public boolean isSourceFilesSavedRequired();

    /**
     * Returns whether the refactoring can be canceled by the user.
     */
    public boolean isCancelable();

    /**
     * Allows to treat the IPS refactoring as LTK refactoring.
     */
    public Refactoring toLtkRefactoring();

    /**
     * Returns the {@link IIpsProject} from which all the elements that are to be refactored
     * originate.
     */
    public IIpsProject getIpsProject();

    /**
     * Method from {@link Refactoring}.
     * 
     * @see Refactoring#getName()
     */
    public String getName();

    /**
     * Method from {@link Refactoring}.
     * 
     * @see Refactoring#checkInitialConditions(IProgressMonitor)
     */
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException,
            OperationCanceledException;

    /**
     * Method from {@link Refactoring}.
     * 
     * @see Refactoring#checkFinalConditions(IProgressMonitor)
     */
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException;

    /**
     * Method from {@link Refactoring}.
     * 
     * @see Refactoring#createChange(IProgressMonitor)
     */
    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException;

}
