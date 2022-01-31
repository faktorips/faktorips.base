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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.IIpsSrcFileEditor;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

public class IpsObjectPartDeleteHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)selection;
            final Set<IIpsSrcFile> srcFilesToSave = new HashSet<>();
            try {
                for (Object o : structuredSelection.toArray()) {
                    IIpsObjectPart objectPart = IpsObjectPartTester.castOrAdaptToPart(o, IIpsObjectPart.class);
                    if (objectPart != null) {
                        deleteIpsObjectPart(event, srcFilesToSave, objectPart);
                    }
                }
            } finally {
                saveSrcFilesAsAJob(srcFilesToSave);
            }
        }
        return null;
    }

    private void saveSrcFilesAsAJob(final Set<IIpsSrcFile> srcFilesToSave) {
        new Job("Save touched Ips-Src-Files") { //$NON-NLS-1$
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                MultiStatus result = new MultiStatus(IpsUIPlugin.PLUGIN_ID, IStatus.OK,
                        "Save touched Ips-Src-Files", null); //$NON-NLS-1$
                new Status(IStatus.OK, IpsUIPlugin.PLUGIN_ID, "Save touched Ips-Src-Files"); //$NON-NLS-1$
                for (IIpsSrcFile srcFileToSave : srcFilesToSave) {
                    try {
                        srcFileToSave.save(true, monitor);
                    } catch (CoreRuntimeException e) {
                        result.add(e.getStatus());
                    }
                }
                return result;
            }
        }.schedule();
    }

    private void deleteIpsObjectPart(ExecutionEvent event,
            final Set<IIpsSrcFile> srcFilesToSave,
            IIpsObjectPart objectPart) {
        IIpsSrcFile srcFile = objectPart.getIpsSrcFile();
        if (IpsUIPlugin.isEditable(srcFile)) {
            if (needToSave(srcFile, event)) {
                srcFilesToSave.add(srcFile);
            }
            objectPart.delete();
        }
    }

    private boolean needToSave(IIpsSrcFile srcFile, ExecutionEvent event) {
        // if file is dirty it is already touched by the user -> never save
        if (srcFile.isDirty()) {
            return false;
        }
        IEditorPart editorPart = HandlerUtil.getActiveEditor(event);
        if (editorPart == null) {
            // action was called from outside an editor -> save should be safe
            return true;
        }
        if (editorPart instanceof IIpsSrcFileEditor) {
            IpsObjectEditor ipsEditor = (IpsObjectEditor)editorPart;
            if (ipsEditor.getIpsSrcFile().equals(srcFile)) {
                // action was called from the editor that's sourcefile we look at -> not save
                return false;
            }
        }
        // action seems to be called from an editor that is not responsible for this srcfile ->
        // better save
        return true;
    }

}
