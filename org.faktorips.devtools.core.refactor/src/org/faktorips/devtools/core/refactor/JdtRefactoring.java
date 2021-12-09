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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.model.abstraction.Abstractions;
import org.faktorips.devtools.model.exception.CoreRuntimeException;

/**
 * A Java refactoring that encapsulates a JDT Java refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class JdtRefactoring extends JavaRefactoring {

    private final Refactoring jdtRefactoring;

    public JdtRefactoring(Refactoring jdtRefactoring) {
        this.jdtRefactoring = jdtRefactoring;
    }

    @Override
    public RefactoringStatus checkAllConditions(IProgressMonitor pm) throws CoreRuntimeException {
        try {
            return jdtRefactoring.checkAllConditions(pm);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    public void perform(final IProgressMonitor pm) throws CoreRuntimeException {
        ICoreRunnable operation = new PerformRefactoringOperation(jdtRefactoring,
                CheckConditionsOperation.FINAL_CONDITIONS);
        Abstractions.getWorkspace().run(operation, pm);
    }

}
