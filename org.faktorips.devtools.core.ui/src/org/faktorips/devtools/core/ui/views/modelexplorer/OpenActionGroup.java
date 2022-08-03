/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelexplorer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.OpenFileAction;
import org.eclipse.ui.actions.OpenInNewWindowAction;
import org.eclipse.ui.actions.OpenWithMenu;
import org.eclipse.ui.ide.ResourceSelectionUtil;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * This is the action group for the open actions.
 */
public class OpenActionGroup extends ActionGroup {

    /**
     * The id for the Open With submenu.
     */
    public static final String OPEN_WITH_ID = IpsPlugin.PLUGIN_ID + ".OpenWithSubMenu"; //$NON-NLS-1$

    private ModelExplorer explorer;
    private OpenFileAction openFileAction;

    public OpenActionGroup(ModelExplorer explorer) {
        this.explorer = explorer;
        makeActions();
    }

    protected void makeActions() {
        openFileAction = new OpenFileAction(explorer.getSite().getPage());
    }

    @Override
    public void fillContextMenu(IMenuManager menu) {
        IStructuredSelection selection = (IStructuredSelection)getContext().getSelection();

        boolean anyResourceSelected = !selection.isEmpty()
                && ResourceSelectionUtil.allResourcesAreOfType(selection, IResource.PROJECT | IResource.FOLDER
                        | IResource.FILE);
        boolean onlyFilesSelected = !selection.isEmpty()
                && ResourceSelectionUtil.allResourcesAreOfType(selection, IResource.FILE);

        if (onlyFilesSelected) {
            openFileAction.selectionChanged(selection);
            menu.add(openFileAction);
            fillOpenWithMenu(menu, selection);
        }

        if (anyResourceSelected) {
            addNewWindowAction(menu, selection);
        }
    }

    /**
     * Adds the OpenWith submenu to the context menu.
     * 
     * @param menu the context menu
     * @param selection the current selection
     */
    private void fillOpenWithMenu(IMenuManager menu, IStructuredSelection selection) {

        // Only supported if exactly one file is selected.
        if (selection.size() != 1) {
            return;
        }
        Object element = selection.getFirstElement();
        if (!(element instanceof IFile)) {
            return;
        }

        MenuManager submenu = new MenuManager(Messages.OpenActionGroup_openWithMenuLabel, OPEN_WITH_ID);
        submenu.add(new OpenWithMenu(explorer.getSite().getPage(), (IFile)element));
        menu.add(submenu);
    }

    /**
     * Adds the Open in New Window action to the context menu.
     * 
     * @param menu the context menu
     * @param selection the current selection
     */
    private void addNewWindowAction(IMenuManager menu, IStructuredSelection selection) {

        // Only supported if exactly one container (i.e open project or folder) is selected.
        if (selection.size() != 1) {
            return;
        }
        Object element = selection.getFirstElement();
        if (!(element instanceof IContainer) || (element instanceof IProject && !(((IProject)element).isOpen()))) {
            return;
        }

        menu.add(new OpenInNewWindowAction(explorer.getSite().getWorkbenchWindow(), (IContainer)element));
    }

    /**
     * Runs the default action (open file).
     */
    public void runDefaultAction(IStructuredSelection selection) {
        Object element = selection.getFirstElement();
        if (element instanceof IFile) {
            openFileAction.selectionChanged(selection);
            openFileAction.run();
        }
    }
}
