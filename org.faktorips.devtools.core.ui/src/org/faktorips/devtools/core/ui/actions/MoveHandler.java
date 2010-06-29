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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.MoveResourceAction;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.wizards.move.MoveWizard;
import org.faktorips.devtools.core.ui.wizards.refactor.MoveRefactoringWizard;

/**
 * Opens the move wizard to allow the user to move a product component or package fragment.
 * 
 * @author Thorsten Guenther
 */
public class MoveHandler extends IpsRefactoringHandler {

    private static final String COMMAND_ID = "org.eclipse.ui.edit.move"; //$NON-NLS-1$

    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
        final Shell shell = HandlerUtil.getActiveShell(event);
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)selection;
            Object selected = structuredSelection.getFirstElement();

            // Open refactoring wizard if supported for selection.
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
                Refactoring refactoring = selectedElement.getMoveRefactoring();
                if (refactoring != null) {

                    // Check initial conditions.
                    try {
                        boolean checkSucceeded = checkInitialConditions(shell, refactoring);
                        if (!(checkSucceeded)) {
                            return null;
                        }
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }

                    // Open the refactoring wizard.
                    RefactoringWizard moveWizard = new MoveRefactoringWizard(refactoring, (IIpsElement)selected);
                    openWizard(shell, moveWizard);

                    return null;
                }

                if (selected instanceof IIpsElement) {
                    MoveWizard move = new MoveWizard(structuredSelection, MoveWizard.OPERATION_MOVE);
                    WizardDialog wd = new WizardDialog(shell, move);
                    wd.open();
                } else if (selected instanceof IResource) {
                    MoveResourceAction action = new MoveResourceAction(new IShellProvider() {
                        public Shell getShell() {
                            return shell;
                        }
                    });
                    action.selectionChanged(structuredSelection);
                    action.run();
                }
            }
        }
        return null;
    }

    public static ContributionItem getContributionItem() {
        CommandContributionItem moveItem = new CommandContributionItem(new CommandContributionItemParameter(PlatformUI
                .getWorkbench(), null, COMMAND_ID, CommandContributionItem.STYLE_PUSH));
        return moveItem;
    }

}
