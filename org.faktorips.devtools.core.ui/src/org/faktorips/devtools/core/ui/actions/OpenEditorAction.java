/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.IGotoIpsObjectPart;

/**
 * Action for opening objects in the corresponding editor.
 * 
 * @author Thorsten Guenther
 * @author Stefan Widmaier
 */
public class OpenEditorAction extends IpsAction {

    public OpenEditorAction(ISelectionProvider selectionProvider) {
        super(selectionProvider);
        super.setText(Messages.OpenEditorAction_name);
        super.setDescription(Messages.OpenEditorAction_description);
        super.setToolTipText(Messages.OpenEditorAction_tooltip);
    }

    @Override
    public void run(IStructuredSelection selection) {
        openEditor(selection);
    }

    public IEditorPart openEditor() {
        ISelection selection = selectionProvider.getSelection();
        if (selection != null) {
            if (selection instanceof IStructuredSelection) {
                return openEditor((IStructuredSelection)selection);
            } else {
                throw new RuntimeException(Messages.IpsAction_msgUnsupportedSelection + selection.getClass().getName());
            }
        }
        return null;
    }

    /**
     * Opens all corresponding editor for the given selection. Returns the editor input of the last
     * opened editor or <code>null</code> if no editor was opened.
     */
    public IEditorPart openEditor(IStructuredSelection selection) {
        // ignores IFiles even if the underlying object is an IpsSrcFile
        IIpsSrcFile[] srcFiles = getIpsSrcFilesForSelection(selection);
        IEditorPart editor = null;
        for (IIpsSrcFile srcFile : srcFiles) {
            editor = IpsUIPlugin.getDefault().openEditor(srcFile);
        }
        editor = openSelectedFiles(selection, editor);
        if (selection.getFirstElement() instanceof IIpsObjectPart && editor instanceof IGotoIpsObjectPart) {
            ((IGotoIpsObjectPart)editor).gotoIpsObjectPart((IIpsObjectPart)selection.getFirstElement());
        }
        return editor;
    }

    /**
     * Open selected files that are no IPS files.
     */
    private IEditorPart openSelectedFiles(IStructuredSelection selection, IEditorPart editor) {
        IEditorPart lastEditor = editor;
        for (Object selectedObject : selection.toArray()) {
            if (selectedObject instanceof IFile) {
                lastEditor = IpsUIPlugin.getDefault().openEditor((IFile)selectedObject);
            }
        }
        return lastEditor;
    }

}
