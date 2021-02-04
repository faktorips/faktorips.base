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

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

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
    public RefactoringStatus checkAllConditions(IProgressMonitor pm) throws CoreException {
        return jdtRefactoring.checkAllConditions(pm);
    }

    @Override
    public void perform(final IProgressMonitor pm) throws CoreException {
        IWorkspaceRunnable operation = new PerformRefactoringOperation(jdtRefactoring,
                CheckConditionsOperation.FINAL_CONDITIONS);
        ResourcesPlugin.getWorkspace().run(operation, pm);
    }

}
