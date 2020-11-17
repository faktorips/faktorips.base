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
import org.faktorips.devtools.core.ui.commands.IpsPasteHandler;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;

/**
 * Action to paste IpsElements or resources.
 * 
 * @author Thorsten Guenther
 */
public class IpsPasteAction extends IpsAction {

    /**
     * The clipboard used to transfer the data
     */
    private Clipboard clipboard;

    /**
     * The shell for this session
     */
    private Shell shell;

    /**
     * Indicates that the new name will be used without a dialog question, if the file already
     * exists
     */
    private boolean forceUseNameSuggestionIfFileExists = false;

    private final IpsPasteHandler pasteHandler = new IpsPasteHandler();

    /**
     * Creates a new action to paste <code>IIpsElement</code>s or resources.
     * 
     * @param selectionProvider The provider for the selection to get the target from.
     * @param shell The shell for this session.
     */
    public IpsPasteAction(ISelectionProvider selectionProvider, Shell shell) {
        super(selectionProvider);
        clipboard = new Clipboard(shell.getDisplay());
        this.shell = shell;
    }

    /**
     * Sets that new name suggestions will be used without interaction with the user (no ui dialog)
     * if there are existing files.
     */
    public void setForceUseNameSuggestionIfFileExists(boolean forceUseNameSuggestionIfFileExists) {
        this.forceUseNameSuggestionIfFileExists = forceUseNameSuggestionIfFileExists;
    }

    @Override
    public void run(IStructuredSelection selection) {
        pasteHandler.pasteFromClipboard(selection, clipboard, shell, forceUseNameSuggestionIfFileExists);
    }

    @Override
    protected boolean computeEnabledProperty(IStructuredSelection selection) {
        // disable action if the selection contains at least one ips object part
        Object[] objects = selection.toArray();
        for (Object object : objects) {
            if (object instanceof IIpsObjectPart) {
                return false;
            }
        }
        return true;
    }

}
