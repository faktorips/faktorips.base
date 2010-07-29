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

package org.faktorips.devtools.core.ui.refactor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.RenameResourceAction;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.refactor.IIpsRefactoringProcessor;
import org.faktorips.devtools.core.ui.actions.Messages;
import org.faktorips.devtools.core.ui.wizards.move.MoveWizard;
import org.faktorips.devtools.core.ui.wizards.refactor.IpsRefactoringDialog;
import org.faktorips.util.ArgumentCheck;

/**
 * Abstract base class for global actions that want to provide refactoring support.
 * <p>
 * This class provides basic functionality common to refactoring actions such as opening a
 * refactoring wizard.
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsRefactoringHandler extends AbstractHandler {

    public static ContributionItem getContributionItem(String commandId) {
        CommandContributionItemParameter parameters = new CommandContributionItemParameter(PlatformUI.getWorkbench(),
                null, commandId, CommandContributionItem.STYLE_PUSH);
        return new CommandContributionItem(parameters);
    }

    protected abstract RefactoringWizard getRefactoringWizard(Refactoring refactoring, IIpsElement selectedIpsElement);

    /**
     * Must return the old move wizard that is still used if the new refactoring support cannot
     * handle a specific situation.
     */
    protected abstract MoveWizard getMoveWizard(IStructuredSelection selection);

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
        final Shell shell = HandlerUtil.getActiveShell(event);
        if (!(selection instanceof IStructuredSelection)) {
            return null;
        }

        IStructuredSelection structuredSelection = (IStructuredSelection)selection;
        Object selected = structuredSelection.getFirstElement();

        // Open refactoring wizard only if the refactoring is supported for the selection.
        if (selected instanceof IIpsElement) {
            IIpsElement selectedElement = (IIpsElement)selected;
            if (selectedElement instanceof IIpsSrcFile) {
                try {
                    selectedElement = ((IIpsSrcFile)selectedElement).getIpsObject();
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                    return null;
                }
            }

            ProcessorBasedRefactoring refactoring = selectedElement.getRenameRefactoring();
            if (refactoring != null) {
                IIpsRefactoringProcessor processor = (IIpsRefactoringProcessor)refactoring.getProcessor();
                IpsCheckConditionsOperation checkConditionsOperation = new IpsCheckConditionsOperation(refactoring,
                        IpsCheckConditionsOperation.INITIAL_CONDITIONS, processor.isSourceFilesSavedRequired(), null);
                try {
                    checkConditionsOperation.run();
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
                if (processor.isSourceFilesSavedRequired()) {
                    if (!(checkConditionsOperation.getEditorsSaved())) {
                        return null;
                    }
                }
                RefactoringStatus status = checkConditionsOperation.getStatus();
                if (!(status.isOK())) {
                    MessageDialog.openInformation(shell, refactoring.getName(),
                            Messages.IpsRefactoringAction_refactoringCurrentlyNotApplicable + "\n\n      - " //$NON-NLS-1$
                                    + status.getEntryWithHighestSeverity().getMessage());
                    return null;
                }
                openRefactoringWizard(shell, getRefactoringWizard(refactoring, selectedElement));
                return null;
            }
        }

        /*
         * Old refactoring code kicking in if the new refactoring support didn't work properly (the
         * function has not returned by this point).
         */
        if (selected instanceof IIpsElement) {
            WizardDialog wd = new WizardDialog(shell, getMoveWizard(structuredSelection));
            wd.open();
        } else if (selected instanceof IResource) {
            RenameResourceAction action = new RenameResourceAction(new IShellProvider() {
                @Override
                public Shell getShell() {
                    return shell;
                }
            });
            action.selectionChanged(structuredSelection);
            action.run();
        }

        return null;
    }

    /**
     * While the wizard is opened the workspace root will be blocked so that work done to the
     * workspace by the refactoring cannot be interrupted.
     */
    private void openRefactoringWizard(Shell shell, RefactoringWizard refactoringWizard) {
        ArgumentCheck.notNull(refactoringWizard);
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

}
