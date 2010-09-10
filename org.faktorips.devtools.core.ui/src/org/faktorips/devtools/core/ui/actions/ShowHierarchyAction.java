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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.views.ipshierarchy.IpsHierarchyView;

public class ShowHierarchyAction extends IpsAction implements IWorkbenchWindowActionDelegate {

    public ShowHierarchyAction() {
        super(null);
    }

    /**
     * The constructor needs a selection provider to get the selected object
     * 
     * @param selectionProvider the selection provider
     */

    public ShowHierarchyAction(ISelectionProvider selectionProvider) {
        super(selectionProvider);
        setText(Messages.ShowHierarchyAction_nameForTypes);
        setDescription(Messages.ShowHierarchyAction_descriptionForTypes);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IpsHierarchyView.LOGO));
    }

    @Override
    public void run(IStructuredSelection selection) {
        IIpsObject ipsObject = getIpsObjectForSelection(selection);
        if (ipsObject == null) {
            return;
        }
        if (IpsHierarchyView.supports(ipsObject)) {
            try {
                IViewPart pse = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage()
                        .showView(IpsHierarchyView.EXTENSION_ID);
                ((IpsHierarchyView)pse).showHierarchy(ipsObject);
            } catch (PartInitException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
    }

    public ISelectionService selectionService;

    @Override
    public void init(IWorkbenchWindow window) {
        selectionService = window.getSelectionService();
    }

    @Override
    public void run(IAction action) {
        run();

    }

    @Override
    public void run() {
        TreeSelection treeSelection = null;
        if (selectionService != null) {
            treeSelection = (TreeSelection)selectionService.getSelection();
        } else {
            treeSelection = (TreeSelection)this.selectionProvider.getSelection();
        }
        IIpsObject ipsObject = getIpsObjectForSelection(treeSelection);
        if (ipsObject == null) {
            return;
        }
        if (IpsHierarchyView.supports(ipsObject)) {
            try {
                IViewPart hierarchyView = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
                        .getActivePage().showView(IpsHierarchyView.EXTENSION_ID);
                ((IpsHierarchyView)hierarchyView).showHierarchy(ipsObject);
            } catch (PartInitException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        // TODO Auto-generated method stub

    }
}
