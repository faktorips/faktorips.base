/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.util.TypedSelection;

public abstract class IpsAbstractHandler extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow activeWindow = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();

        TypedSelection<IAdaptable> typedSelection;

        IWorkbenchPage activePage = activeWindow.getActivePage();

        IWorkbenchPart part = activeWindow.getPartService().getActivePart();
        if (part instanceof IEditorPart) {
            typedSelection = getSelectionFromEditor(part);
        } else {
            typedSelection = getSelectionFromSelectionProvider();
        }
        if (typedSelection == null || !typedSelection.isValid()) {
            return null;
        }
        IIpsSrcFile ipsSrcFile = (IIpsSrcFile)typedSelection.getFirstElement().getAdapter(IIpsSrcFile.class);
        if (ipsSrcFile == null) {
            return null;
        }
        showView(activePage, ipsSrcFile);
        // return must be null - see jdoc
        return null;
    }

    protected TypedSelection<IAdaptable> getSelectionFromSelectionProvider() {
        TypedSelection<IAdaptable> typedSelection;
        ISelectionService selectionService = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
                .getSelectionService();
        typedSelection = new TypedSelection<IAdaptable>(IAdaptable.class, selectionService.getSelection());
        return typedSelection;
    }

    protected TypedSelection<IAdaptable> getSelectionFromEditor(IWorkbenchPart part) {
        IEditorInput input = ((IEditorPart)part).getEditorInput();
        if (input instanceof IFileEditorInput) {
            return new TypedSelection<IAdaptable>(IAdaptable.class, new StructuredSelection(((IFileEditorInput)input)
                    .getFile()));
        } else {
            return null;
        }
    }

    public abstract void showView(IWorkbenchPage activePage, IIpsSrcFile ipsSrcFile);
}
