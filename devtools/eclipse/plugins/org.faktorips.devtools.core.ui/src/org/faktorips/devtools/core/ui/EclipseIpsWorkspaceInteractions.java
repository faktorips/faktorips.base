/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.faktorips.devtools.model.plugin.IIpsWorkspaceInteractions;

public class EclipseIpsWorkspaceInteractions implements IIpsWorkspaceInteractions {

    @Override
    public void runInDisplayThreadAsyncIfNotCurrentDisplay(Runnable runnable) {
        if (PlatformUI.isWorkbenchRunning() && Display.getCurrent() == null) {
            // only run in asynchronous display thread if we are not already in display thread.
            PlatformUI.getWorkbench().getDisplay().asyncExec(runnable);
        } else {
            runnable.run();
        }
    }

    @Override
    public void runInDisplayThreadSync(Runnable runnable) {
        IpsUIPlugin.getDefault().getWorkbench().getDisplay().syncExec(runnable);
    }

    @Override
    public void runInDisplayThreadAsync(Runnable runnable) {
        Display display = Display.getCurrent() != null ? Display.getCurrent() : Display.getDefault();
        display.asyncExec(runnable);
    }

    @Override
    public void showErrorDialog(IStatus status) {
        runInDisplayThreadAsync(() -> ErrorDialog.openError(Display.getDefault().getActiveShell(),
                Messages.IpsUIPlugin_titleErrorDialog, Messages.IpsUIPlugin_msgUnexpectedError,
                status));
    }

}
