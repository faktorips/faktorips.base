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

import java.util.LinkedHashSet;
import java.util.Set;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.RenameResourceAction;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
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

    protected abstract IpsRefactoringWizard getRefactoringWizard(IIpsRefactoring refactoring);

    /**
     * Must return the old move wizard that is still used if the new refactoring support cannot
     * handle a specific situation.
     */
    protected abstract MoveWizard getMoveWizard(IStructuredSelection selection);

    /**
     * Must return the refactoring instance for the selected IPS elements.
     * <p>
     * May return null if the refactoring is not possible.
     */
    protected abstract IIpsRefactoring getRefactoring(Set<IIpsElement> selectedIpsElements);

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
        final Shell shell = HandlerUtil.getActiveShell(event);
        if (!(selection instanceof IStructuredSelection)) {
            return null;
        }

        IStructuredSelection structuredSelection = (IStructuredSelection)selection;
        Set<IIpsElement> selectedIpsElements = new LinkedHashSet<IIpsElement>(structuredSelection.size());
        for (Object selectedElement : structuredSelection.toArray()) {
            if (!(selectedElement instanceof IIpsElement)) {
                break;
            }
            IIpsElement selectedIpsElement = (IIpsElement)selectedElement;
            if (selectedIpsElement instanceof IIpsSrcFile) {
                try {
                    selectedIpsElement = ((IIpsSrcFile)selectedIpsElement).getIpsObject();
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                    return null;
                }
            }
            selectedIpsElements.add(selectedIpsElement);
        }

        // Open refactoring wizard only if the refactoring is supported for the selection
        if (selectedIpsElements.size() == structuredSelection.size()) {
            IIpsRefactoring refactoring = getRefactoring(selectedIpsElements);
            if (refactoring != null) {
                IpsRefactoringOperation refactoringOperation = new IpsRefactoringOperation(refactoring, shell);
                IpsRefactoringWizard refactoringWizard = getRefactoringWizard(refactoring);
                refactoringOperation.runWizardInteraction(refactoringWizard);
                return null;
            }
        }

        /*
         * Old refactoring code kicking in if the new refactoring support didn't work properly (the
         * function has not returned by this point).
         */
        Object selected = structuredSelection.getFirstElement();
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
