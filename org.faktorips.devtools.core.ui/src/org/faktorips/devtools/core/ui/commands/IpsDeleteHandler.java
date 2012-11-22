/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.DeleteResourceAction;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.util.ArgumentCheck;

public class IpsDeleteHandler extends IpsAbstractHandler {

    @Override
    public void execute(ExecutionEvent event, IWorkbenchPage activePage, IIpsSrcFile ipsSrcFile)
            throws ExecutionException {
        final Shell shell = HandlerUtil.getActiveShell(event);
        ArgumentCheck.notNull(shell, this);
        DeleteResourceAction deleteAction = new DeleteResourceAction(new IShellProvider() {

            @Override
            public Shell getShell() {
                return shell;
            }
        });
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            deleteAction.selectionChanged((IStructuredSelection)selection);
        }
        if (canDelete((IStructuredSelection)selection)) {
            deleteAction.run();
        }
    }

    private boolean canDelete(IStructuredSelection selection) {
        Object[] items = selection.toArray();
        boolean canDelete = true;
        for (Object item : items) {
            if (item instanceof IAdaptable && ((IAdaptable)item).getAdapter(IIpsObjectPart.class) != null) {
                canDelete = false;
            }
        }
        return canDelete;
    }

}