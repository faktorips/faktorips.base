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

import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;

/**
 * Faktor-IPS refactoring that allows external refactoring participants to commit to the
 * refactoring.
 * 
 * @see ProcessorBasedRefactoring
 * 
 * @author Alexander Weickmann
 */
public interface IIpsProcessorBasedRefactoring extends IIpsRefactoring {

    /**
     * Returns the {@link IpsRefactoringProcessor} that is associated with this processor-based
     * refactoring.
     */
    public IpsRefactoringProcessor getIpsRefactoringProcessor();

    /**
     * Allows to treat the IPS processor-based refactoring as LTK processor-based refactoring.
     */
    @Override
    public ProcessorBasedRefactoring toLtkRefactoring();

    /**
     * {@inheritDoc}
     * <p>
     * Processor-based refactorings can not be canceled at the moment but this may change in the
     * future.
     */
    @Override
    public boolean isCancelable();

}
