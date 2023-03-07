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

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ltk.ui.refactoring.resource.RenameResourceWizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.ui.wizards.refactor.IpsRefactoringWizard;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * Abstract base class for global actions that want to provide refactoring support.
 * <p>
 * This class provides basic functionality common to refactoring actions such as opening a
 * refactoring wizard.
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsRefactoringHandler extends AbstractHandler {

    public static ContributionItem getContributionItem(String commandId, String label) {
        // @formatter:off
        // CSOFF: TrailingCommentCheck
        CommandContributionItemParameter parameters = new CommandContributionItemParameter(
                PlatformUI.getWorkbench(),              // serviceLocator
                null,                                   // id
                commandId,                              // commandId
                null,                                   // parameters
                null,                                   // icon
                null,                                   // disabledIcon
                null,                                   // hoverIcon
                label,                                  // label
                null,                                   // mnemoic
                null,                                   // tooltip
                CommandContributionItem.STYLE_PUSH,     // style
                null,                                   // helpContextId
                false                                   // visibleEnabled
                );
        // CSON: TrailingCommentCheck
        // @formatter:on
        return new CommandContributionItem(parameters);
    }

    /**
     * Must return the refactoring wizard that shall be used to guide the user trough the
     * refactoring.
     * <p>
     * Won't be called if {@link #getRefactoring(Set)} returns null.
     */
    protected abstract IpsRefactoringWizard getRefactoringWizard(IIpsRefactoring refactoring);

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
        if (!(selection instanceof IStructuredSelection structuredSelection)) {
            return null;
        }

        Set<IIpsElement> selectedIpsElements = new LinkedHashSet<>(structuredSelection.size());
        for (Object selectedElement : structuredSelection.toArray()) {
            if (!(selectedElement instanceof IAdaptable)) {
                break;
            }
            IIpsElement selectedIpsElement = ((IAdaptable)selectedElement).getAdapter(IIpsElement.class);
            if (selectedIpsElement == null) {
                break;
            }
            if (selectedIpsElement instanceof IIpsSrcFile) {
                selectedIpsElement = ((IIpsSrcFile)selectedIpsElement).getIpsObject();
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

        // all other sources
        Object selected = structuredSelection.getFirstElement();
        if (selected instanceof IResource) {
            RenameResourceWizard refactoringWizard = new RenameResourceWizard((IResource)selected);
            RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(refactoringWizard);
            try {
                op.run(shell, Messages.IpsRefactoringHandler_renameResource);
            } catch (InterruptedException e) {
                // do nothing
            }
        }

        return null;
    }

}
