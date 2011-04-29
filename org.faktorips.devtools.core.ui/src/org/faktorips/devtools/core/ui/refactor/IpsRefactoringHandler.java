/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.RenameResourceAction;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.wizards.move.MoveWizard;
import org.faktorips.devtools.core.ui.wizards.refactor.IpsRefactoringWizard;

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

    protected abstract IpsRefactoringWizard getRefactoringWizard(Refactoring refactoring, IIpsElement selectedIpsElement);

    /**
     * Must return the old move wizard that is still used if the new refactoring support cannot
     * handle a specific situation.
     */
    protected abstract MoveWizard getMoveWizard(IStructuredSelection selection);

    /**
     * Must return the refactoring instance for the given selected IPS element.
     */
    protected abstract ProcessorBasedRefactoring getRefactoring(IIpsElement selectedElement);

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

            ProcessorBasedRefactoring refactoring = getRefactoring(selectedElement);
            if (refactoring != null) {
                IpsRefactoringOperation refactoringOperation = new IpsRefactoringOperation(refactoring, shell);
                refactoringOperation.runWizardInteraction(getRefactoringWizard(refactoring, selectedElement));
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

}
