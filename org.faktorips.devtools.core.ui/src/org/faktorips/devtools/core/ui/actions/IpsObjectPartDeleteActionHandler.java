/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.IIpsSrcFileEditor;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;

public class IpsObjectPartDeleteActionHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)selection;
            final Set<IIpsSrcFile> srcFilesToSave = new HashSet<IIpsSrcFile>();
            try {
                for (Object o : structuredSelection.toArray()) {
                    if (o instanceof IIpsObjectPart) {
                        IIpsObjectPart objectPart = (IIpsObjectPart)o;
                        IIpsSrcFile srcFile = objectPart.getIpsSrcFile();
                        if (IpsUIPlugin.isEditable(srcFile)) {
                            if (needToSave(srcFile, event)) {
                                srcFilesToSave.add(srcFile);
                            }
                            objectPart.delete();
                        }
                    }
                }
            } finally {
                new Job("Save touched Ips-Src-Files") {

                    @Override
                    protected IStatus run(IProgressMonitor monitor) {
                        MultiStatus result = new MultiStatus(IpsUIPlugin.PLUGIN_ID, IStatus.OK,
                                "Save touched Ips-Src-Files", null);
                        new Status(IStatus.OK, IpsUIPlugin.PLUGIN_ID, "Save touched Ips-Src-Files");
                        for (IIpsSrcFile srcFileToSave : srcFilesToSave) {
                            try {
                                srcFileToSave.save(true, monitor);
                            } catch (CoreException e) {
                                result.add(e.getStatus());
                            }
                        }
                        return result;
                    }
                }.schedule();
            }
        }
        return null;
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
                // action was called from the editor that's sourcefile we look at -> better not safe
                return false;
            }
        }
        // action seems to be called from an editor that is not responsible for this srcfile ->
        // better safe
        return true;
    }

}
