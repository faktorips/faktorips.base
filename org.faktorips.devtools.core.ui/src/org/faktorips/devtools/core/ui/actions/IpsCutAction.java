/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.commands.IpsCutHandler;

/**
 * An action to cut IpsObjectPartContainer-objects out of the model into the clipboard.
 * 
 * @author Thorsten Guenther
 */
public class IpsCutAction extends IpsAction {

    private Clipboard clipboard;
    private final IpsCutHandler cutHandler = new IpsCutHandler();

    public IpsCutAction(ISelectionProvider selectionProvider, Shell shell) {
        super(selectionProvider);
        clipboard = new Clipboard(shell.getDisplay());
    }

    @Override
    public void run(IStructuredSelection selection) {
        cutHandler.cutToClipboard(selection, clipboard);
    }
}
