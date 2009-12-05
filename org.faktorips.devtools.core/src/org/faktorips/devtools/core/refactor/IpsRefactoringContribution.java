/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;

/**
 * An <tt>IpsRefactoringContribution</tt> makes it possible to start a refactoring head-less. Every
 * contribution must be registered to the platform by using the LTK <tt>refactoringContribution</tt>
 * extension point.
 * 
 * @see IpsRefactoringDescriptor
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsRefactoringContribution extends RefactoringContribution {

    /**
     * Creates the refactoring published by this contribution. The given
     * <tt>IpsRefactoringDescriptor</tt> is used to initialize the refactoring.
     * 
     * @param descriptor An <tt>IpsRefactoringDescriptor</tt> that is used to initialize the
     *            refactoring.
     * 
     * @throws CoreException If any error occurs during the creation of the refactoring.
     * @throws NullPointerException If <tt>descriptor</tt> is <tt>null</tt>.
     */
    public abstract Refactoring createRefactoring(IpsRefactoringDescriptor descriptor) throws CoreException;

}
