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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.ui.actions.IpsUpdateValidfromAction;
import org.faktorips.devtools.core.ui.commands.IpsAbstractHandler;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

public class IpsUpdateValidfromHandler extends IpsAbstractHandler {

    public static final String CONTRIBUTION_ID = "org.faktorips.devtools.core.refactor.updateValidfrom"; //$NON-NLS-1$

    @Override
    public void execute(ExecutionEvent event, IWorkbenchPage activePage, IIpsSrcFile ipsSrcFile)
            throws ExecutionException {
        Shell activeShell = HandlerUtil.getActiveShell(event);
        ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
        if (currentSelection instanceof IStructuredSelection) {
            new IpsUpdateValidfromAction(activeShell, null)
                    .run((IStructuredSelection)currentSelection);
        }

    }

}
