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

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.core.DisplayProvider;

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
    public void perform(final IProgressMonitor pm) {
        // We had random exceptions on some machines when not using the user-interface thread
        DisplayProvider.syncExec(new Runnable() {
            @Override
            public void run() {
                IWorkspaceRunnable operation = new PerformRefactoringOperation(jdtRefactoring,
                        CheckConditionsOperation.FINAL_CONDITIONS);
                try {
                    operation.run(pm);
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

}
