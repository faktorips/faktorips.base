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
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class CopyTableAction extends IpsAction {

    private final Shell shell;

    public CopyTableAction(Shell shell, ISelectionProvider selectionProvider) {
        super(selectionProvider);
        this.shell = shell;
        setText(Messages.CopyTableAction_title);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("NewTableContentsCopy.gif")); //$NON-NLS-1$
    }

    @Override
    public void run(IStructuredSelection selection) {
        IpsCopyAction copyAction = new IpsCopyAction(selectionProvider, shell);
        copyAction.run();
        IpsPasteAction pasteAction = new IpsPasteAction(selectionProvider, shell);
        pasteAction.run();
    }

}
