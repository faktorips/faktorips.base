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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

public abstract class IpsAbstractHandler extends AbstractHandler {

    protected static final String ARCHIVE_LINK = "ARCHIVE_LINK"; //$NON-NLS-1$

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IIpsSrcFile ipsSrcFile = getCurrentlySelectedIpsSrcFile();
        if (ipsSrcFile == null) {
            return null;
        }

        IWorkbenchWindow activeWindow = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage activePage = activeWindow.getActivePage();
        execute(event, activePage, ipsSrcFile);

        // return must be null - see jdoc
        return null;
    }

    protected TypedSelection<IAdaptable> getSelectionFromSelectionProvider() {
        ISelectionService selectionService = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
                .getSelectionService();
        ISelection selection = selectionService.getSelection();
        return TypedSelection.createAnyCount(IAdaptable.class, selection);
    }

    protected TypedSelection<IAdaptable> getSelectionFromEditor(IWorkbenchPart part) {
        IEditorInput input = ((IEditorPart)part).getEditorInput();
        if (input instanceof IFileEditorInput) {
            return new TypedSelection<>(IAdaptable.class,
                    new StructuredSelection(((IFileEditorInput)input).getFile()));
        } else {
            return null;
        }
    }

    public abstract void execute(ExecutionEvent event, IWorkbenchPage activePage, IIpsSrcFile ipsSrcFile)
            throws ExecutionException;

    protected IIpsSrcFile getCurrentlySelectedIpsSrcFile() {
        IWorkbenchWindow activeWindow = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();

        IWorkbenchPart part = activeWindow.getPartService().getActivePart();
        TypedSelection<IAdaptable> typedSelection;
        if (part instanceof IEditorPart) {
            typedSelection = getSelectionFromEditor(part);
        } else {
            typedSelection = getSelectionFromSelectionProvider();
        }
        if (typedSelection == null || !typedSelection.isValid()) {
            return null;
        }

        return typedSelection.getFirstElement().getAdapter(IIpsSrcFile.class);
    }

}
