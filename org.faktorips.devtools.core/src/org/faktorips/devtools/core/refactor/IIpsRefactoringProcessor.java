/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * An <tt>IIpsRefactoringProcessor</tt> implements a specific Faktor-IPS refactoring.
 * 
 * @author Alexander Weickmann
 */
public interface IIpsRefactoringProcessor {

    /**
     * Responsible for validating the user input.
     * 
     * @param pm An <tt>IProgressMonitor</tt> to report progress to.
     * 
     * @throws CoreException If an error occurs while validating the user input.
     */
    public abstract RefactoringStatus validateUserInput(IProgressMonitor pm) throws CoreException;

}
