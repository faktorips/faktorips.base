/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Abstract base class for refactorings that modify Java code.
 * <p>
 * This class was introduced to have a type that describes Java modifications. A Java modification
 * usually is realized as JDT refactoring but we also do custom modifications, e.g. copying methods
 * from interface to interface for the pull up refactoring (as JDT does not support this).
 * 
 * @author Alexander Weickmann
 */
public abstract class JavaRefactoring {

    /**
     * Checks the initial and final conditions of this Java refactoring and returns an appropriate
     * status object.
     * 
     * @param pm The progress monitor to report progress to or null if no progress reporting is
     *            needed
     * 
     * @throws CoreException May throw this kind of exception if an error occurs
     */
    public abstract RefactoringStatus checkAllConditions(final IProgressMonitor pm) throws CoreException;

    /**
     * Executes this Java refactoring.
     * 
     * @param pm The progress monitor to report progress to or null if no progress reporting is
     *            needed
     */
    public abstract void perform(final IProgressMonitor pm);

}
