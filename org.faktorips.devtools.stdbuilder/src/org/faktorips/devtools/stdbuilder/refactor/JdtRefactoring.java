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
        ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                IWorkspaceRunnable operation = new PerformRefactoringOperation(jdtRefactoring,
                        CheckConditionsOperation.FINAL_CONDITIONS);
                operation.run(pm);
            }
        }, pm);
    }

}
