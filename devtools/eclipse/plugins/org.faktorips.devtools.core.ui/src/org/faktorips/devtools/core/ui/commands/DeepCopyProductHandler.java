/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.ui.actions.IpsDeepCopyAction;
import org.faktorips.devtools.core.ui.wizards.deepcopy.DeepCopyWizard;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

public class DeepCopyProductHandler extends IpsAbstractHandler {

    @Override
    public void execute(ExecutionEvent event, IWorkbenchPage activePage, IIpsSrcFile ipsSrcFile)
            throws ExecutionException {
        Shell activeShell = HandlerUtil.getActiveShell(event);
        ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
        if (currentSelection instanceof IStructuredSelection) {
            new IpsDeepCopyAction(activeShell, null, DeepCopyWizard.TYPE_COPY_PRODUCT)
                    .run((IStructuredSelection)currentSelection);
        }
    }

}
