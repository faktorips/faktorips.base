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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.DeleteResourceAction;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.util.ArgumentCheck;

public class ModelExplorerDeleteAction extends IpsAction implements IShellProvider {

    private DeleteResourceAction deleteAction;
    private Shell shell;

    public ModelExplorerDeleteAction(ISelectionProvider provider, Shell shell) {
        super(provider);
        ArgumentCheck.notNull(shell, this);
        this.shell = shell;
        deleteAction = new DeleteResourceAction(this);
        provider.addSelectionChangedListener(deleteAction);
        ISelection selection = provider.getSelection();
        if (selection instanceof IStructuredSelection) {
            deleteAction.selectionChanged((IStructuredSelection)selection);
        }
    }

    private boolean canDelete(IStructuredSelection selection) {
        Object[] items = selection.toArray();
        boolean canDelete = true;
        for (Object item : items) {
            if (item instanceof IIpsObjectPart) {
                canDelete = false;
            }
        }
        return canDelete;
    }

    @Override
    public void dispose() {
        super.dispose();
        selectionProvider.removeSelectionChangedListener(deleteAction);
    }

    @Override
    protected boolean computeEnabledProperty(IStructuredSelection selection) {
        return canDelete(selection);
    }

    @Override
    public void run(IStructuredSelection selection) {
        if (canDelete(selection)) {
            deleteAction.run();
        }
    }

    @Override
    public Shell getShell() {
        return shell;
    }

}
