/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.refactor;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.RefactoringUI;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.ui.actions.Messages;
import org.faktorips.devtools.core.ui.wizards.refactor.IpsRefactoringDialog;
import org.faktorips.devtools.core.ui.wizards.refactor.IpsRefactoringWizard;
import org.faktorips.util.ArgumentCheck;

/**
 * This operation enables the client to execute a Faktor-IPS refactoring. There are two ways to
 * start a refactoring:
 * <ol>
 * <li>Directly execute a fully configured refactoring instance.</li>
 * <li>Start a refactoring wizard and configure the refactoring trough user interaction.</li>
 * </ol>
 * <p>
 * When executing a fully configured refactoring instance directly all preconditions are evaluated
 * first. If there are any warnings or errors an appropriate dialog is displayed to the user who may
 * then choose either to cancel the refactoring or to proceed. The refactoring progress is shown
 * trough a progress dialog.
 * <p>
 * When starting a refactoring wizard only the initial conditions are evaluated. If there are any
 * errors an appropriate dialog is displayed to the user informing him that the refactoring cannot
 * be started. A refactoring wizard is opened only if the initial conditions are OK.
 * <p>
 * In any case it is checked if all opened editors are saved if the refactoring requires this. If
 * the check fails a 'Save all editors' dialog is presented to the user:
 * <ol>
 * <li>If the user cancels the dialog the whole refactoring is canceled.</li>
 * <li>If the user only saves some of the opened editors or chooses to not save any opened editors
 * at all then the refactoring will be executed / started anyways (assuming the user knows what he
 * does).</li>
 * <li>If the user saves all resources there is no problem at all.</li>
 * </ol>
 * 
 * @author Alexander Weickmann
 */
public class IpsRefactoringOperation {

    /** The refactoring instance to execute. */
    private final IIpsRefactoring refactoring;

    /** The UI shell wherein this refactoring is being executed. */
    private final Shell shell;

    /**
     * @param refactoring The refactoring to execute
     * @param shell The UI shell wherein this refactoring is being executed
     * 
     * @throws NullPointerException If any parameter is null
     */
    public IpsRefactoringOperation(IIpsRefactoring refactoring, Shell shell) {
        ArgumentCheck.notNull(new Object[] { refactoring, shell });
        this.refactoring = refactoring;
        this.shell = shell;
    }

    /**
     * Starts the refactoring by directly executing the refactoring instance assuming the
     * refactoring instance is fully configured.
     */
    public void runDirectExecution() {
        boolean conditionsOk = checkConditionsDirectExecution();
        if (!(conditionsOk)) {
            return;
        }
        performRefactoringDirectExecution();
    }

    /**
     * Starts the refactoring by opening a refactoring wizard assuming the refactoring instance is
     * not yet fully configured and user interaction is required.
     */
    public void runWizardInteraction(IpsRefactoringWizard refactoringWizard) {
        RefactoringStatus status = new RefactoringStatus();
        boolean conditionsOk = checkConditionsWizardInteraction(status);
        if (!(conditionsOk)) {
            if (!status.isOK()) {
                MessageDialog.openInformation(shell, refactoring.getName(),
                        Messages.IpsRefactoringAction_refactoringCurrentlyNotApplicable + "\n\n      - " //$NON-NLS-1$
                                + status.getEntryWithHighestSeverity().getMessage());
            }
            return;
        }
        performRefactoringWizardInteraction(refactoringWizard);
    }

    private void performRefactoringDirectExecution() {
        final PerformRefactoringOperation performOperation = new PerformRefactoringOperation((Refactoring)refactoring,
                CheckConditionsOperation.ALL_CONDITIONS);
        try {
            ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
            dialog.run(true, refactoring.isCancelable(), monitor -> {
                try {
                    ResourcesPlugin.getWorkspace().run(performOperation, monitor);
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            // Operation canceled, ignore exception
        }

        final RefactoringStatus status = performOperation.getConditionStatus();
        if (status.hasError()) {
            // Just to show the error to the user
            requestUserConfirmation(status);
        }
    }

    private void performRefactoringWizardInteraction(IpsRefactoringWizard refactoringWizard) {
        IJobManager jobManager = Job.getJobManager();
        jobManager.beginRule(ResourcesPlugin.getWorkspace().getRoot(), null);
        try {
            Dialog dialog = new IpsRefactoringDialog(shell, refactoringWizard);
            dialog.create();
            dialog.open();
        } finally {
            jobManager.endRule(ResourcesPlugin.getWorkspace().getRoot());
        }
    }

    private boolean checkConditionsDirectExecution() {
        final IpsCheckConditionsOperation checkConditionsOperation = new IpsCheckConditionsOperation(refactoring,
                IpsCheckConditionsOperation.ALL_CONDITIONS, refactoring.isSourceFilesSavedRequired());
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
        try {
            dialog.run(true, refactoring.isCancelable(), monitor -> {
                checkConditionsOperation.run(monitor);
            });
        } catch (InvocationTargetException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (refactoring.isSourceFilesSavedRequired()) {
            if (!(checkConditionsOperation.getEditorsSaved())) {
                return false;
            }
        }
        RefactoringStatus status = checkConditionsOperation.getStatus();
        if (status.hasWarning()) {
            return requestUserConfirmation(status);
        }
        return true;
    }

    private boolean checkConditionsWizardInteraction(RefactoringStatus status) {
        IpsCheckConditionsOperation checkConditionsOperation = new IpsCheckConditionsOperation(refactoring,
                IpsCheckConditionsOperation.INITIAL_CONDITIONS, refactoring.isSourceFilesSavedRequired());
        checkConditionsOperation.run(null);
        if (refactoring.isSourceFilesSavedRequired()) {
            if (!(checkConditionsOperation.getEditorsSaved())) {
                return false;
            }
        }
        status.merge(checkConditionsOperation.getStatus());
        return status.isOK();
    }

    /**
     * Opens up a dialog informing the user of the refactoring status and requiring confirmation to
     * proceed with the refactoring.
     */
    private boolean requestUserConfirmation(RefactoringStatus status) {
        final Dialog dialog = RefactoringUI.createRefactoringStatusDialog(status, shell, "Refactoring Status", false); //$NON-NLS-1$
        final int[] result = new int[1];
        Runnable openDialogRunnable = () -> result[0] = dialog.open();
        shell.getDisplay().syncExec(openDialogRunnable);
        return result[0] == IDialogConstants.OK_ID;
    }

}
