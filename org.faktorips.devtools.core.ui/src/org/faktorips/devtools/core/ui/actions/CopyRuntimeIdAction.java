/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.commands.CopyRuntimeIdHandler;

public class CopyRuntimeIdAction extends IpsAction {

    private Clipboard clipboard;
    private final CopyRuntimeIdHandler copyHandler = new CopyRuntimeIdHandler();

    public CopyRuntimeIdAction(ISelectionProvider selectionProvider, Shell shell) {
        super(selectionProvider);
        setText(Messages.CopyRuntimeId_name);
        setDescription(Messages.CopyRuntimeId_description);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("CopyRuntimeId.gif")); //$NON-NLS-1$
        clipboard = new Clipboard(shell.getDisplay());
    }

    @Override
    public void run(IStructuredSelection selection) {
        copyHandler.copyRuntimeIdToClipboard(selection, clipboard);
    }
}
