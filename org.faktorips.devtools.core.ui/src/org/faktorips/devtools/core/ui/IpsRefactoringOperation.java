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

package org.faktorips.devtools.core.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.RefactoringUI;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.util.ArgumentCheck;

/**
 * This operation enables the client to execute a refactoring. The status of the condition checking
 * phase is evaluated. If any warning or error is detected, an appropriate dialog is displayed to
 * the user who may then choose either to cancel the refactoring or to proceed.
 * 
 * @author Alexander Weickmann
 */
public final class IpsRefactoringOperation {

    /** The refactoring to execute. */
    private final Refactoring refactoring;

    /** The UI shell wherein this refactoring is being executed. */
    private final Shell shell;

    /**
     * @param refactoring The refactoring to execute.
     * @param shell The UI shell wherein this refactoring is being executed.
     * 
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    public IpsRefactoringOperation(Refactoring refactoring, Shell shell) {
        ArgumentCheck.notNull(new Object[] { refactoring, shell });
        this.refactoring = refactoring;
        this.shell = shell;
    }

    /**
     * Executes the refactoring. The conditions will be checked first. An appropriate dialog will be
     * shown to the user in case any warnings or errors are identified during the check.
     */
    public void execute() {
        boolean conditionsOk = checkConditions();
        if (!(conditionsOk)) {
            return;
        }

        performRefactoring();
    }

    private void performRefactoring() {
        final PerformRefactoringOperation performOperation = new PerformRefactoringOperation(refactoring,
                CheckConditionsOperation.ALL_CONDITIONS);
        try {
            ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
            dialog.run(true, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) {
                    try {
                        ResourcesPlugin.getWorkspace().run(performOperation, monitor);
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        final RefactoringStatus status = performOperation.getConditionStatus();
        if (status.hasError()) {
            // Just to show the error to the user.
            requestUserConfirmation(status);
        }
    }

    /** Checks the refactoring's conditions and returns whether it is OK to execute the refactoring. */
    private boolean checkConditions() {
        CheckConditionsOperation checkOperation = new CheckConditionsOperation(refactoring,
                CheckConditionsOperation.ALL_CONDITIONS);
        try {
            checkOperation.run(new NullProgressMonitor());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
        RefactoringStatus checkStatus = checkOperation.getStatus();
        if (checkStatus.hasWarning()) {
            return requestUserConfirmation(checkStatus);
        }
        return true;
    }

    /** Opens up a dialog and asks the user whether to proceed with the refactoring. */
    private boolean requestUserConfirmation(RefactoringStatus status) {
        final Dialog dialog = RefactoringUI.createRefactoringStatusDialog(status, shell, "Refactoring Status", false); //$NON-NLS-1$
        final int[] result = new int[1];
        Runnable openDialogRunnable = new Runnable() {
            @Override
            public void run() {
                result[0] = dialog.open();
            }
        };
        shell.getDisplay().syncExec(openDialogRunnable);
        return result[0] == IDialogConstants.OK_ID;
    }

}
