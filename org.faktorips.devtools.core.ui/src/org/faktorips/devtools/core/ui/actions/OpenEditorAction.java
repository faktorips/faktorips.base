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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.IGotoIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * Action for opening objects in the corresponding editor.
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
     * Opens all corresponding editor for the given selection. Returns the most recently opened
     * editor or <code>null</code> if no editor was opened.
     */
    public IEditorPart openEditor(IStructuredSelection selection) {
        IEditorPart ipsEditor = openEditorsForIpsSrcFiles(selection);
        handleGotoIpsObjectPart(selection, ipsEditor);
        IEditorPart fileEditor = openEditorsForIFiles(selection);
        return fileEditor != null ? fileEditor : ipsEditor;
    }

    /**
     * Opens the respective editors for all IPSSrcFiles in the selection.
     * 
     * @return the most recently opened editor
     * 
     */
    private IEditorPart openEditorsForIpsSrcFiles(IStructuredSelection selection) {
        // ignores IFiles even if the underlying object is an IpsSrcFile
        IEditorPart mostRecentlyOpenedEditor = null;
        IIpsSrcFile[] srcFiles = getIpsSrcFilesForSelection(selection);
        for (IIpsSrcFile srcFile : srcFiles) {
            mostRecentlyOpenedEditor = IpsUIPlugin.getDefault().openEditor(srcFile);
        }
        return mostRecentlyOpenedEditor;
    }

    /**
     * Opens the respective editors for all IFiles in the selection.
     * 
     * @return the most recently opened editor
     * 
     */
    private IEditorPart openEditorsForIFiles(IStructuredSelection selection) {
        IEditorPart mostRecentlyOpenedEditor = null;
        for (Object selectedObject : selection.toArray()) {
            if (selectedObject instanceof IFile) {
                mostRecentlyOpenedEditor = IpsUIPlugin.getDefault().openEditor((IFile)selectedObject);
            }
        }
        return mostRecentlyOpenedEditor;
    }

    private void handleGotoIpsObjectPart(IStructuredSelection selection, IEditorPart editor) {
        if (isFirstElementAPart(selection) && supportsGoto(editor)) {
            ((IGotoIpsObjectPart)editor).gotoIpsObjectPart((IIpsObjectPart)selection.getFirstElement());
        }
    }

    private boolean supportsGoto(IEditorPart editor) {
        return editor instanceof IGotoIpsObjectPart;
    }

    private boolean isFirstElementAPart(IStructuredSelection selection) {
        return selection.getFirstElement() instanceof IIpsObjectPart;
    }

}
