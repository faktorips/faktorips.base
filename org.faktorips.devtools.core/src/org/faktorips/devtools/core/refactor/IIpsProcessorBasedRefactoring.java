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
    IpsRefactoringProcessor getIpsRefactoringProcessor();

    /**
     * Allows to treat the IPS processor-based refactoring as LTK processor-based refactoring.
     */
    @Override
    ProcessorBasedRefactoring toLtkRefactoring();

    /**
     * {@inheritDoc}
     * <p>
     * Processor-based refactorings can not be canceled at the moment but this may change in the
     * future.
     */
    @Override
    boolean isCancelable();

}
