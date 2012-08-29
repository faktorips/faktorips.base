/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.core.ui.views.modeloverview.IpsModelOverviewView;

public class ShowModelOverviewHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // this method must be called first! Otherwise the selection is not valid.
        TypedSelection<IAdaptable> selection = getSelectionFromSelectionProvider();

        IWorkbenchWindow activeWindow = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage activePage = activeWindow.getActivePage();

        try {
            IViewPart modelOverviewView = activePage.showView(IpsModelOverviewView.EXTENSION_ID, null,
                    IWorkbenchPage.VIEW_ACTIVATE);
            IAdaptable firstElement = selection.getFirstElement();
            if (selection.getFirstElement() instanceof IpsSrcFile) {
                try {
                    IpsSrcFile element = (IpsSrcFile)firstElement;
                    ((IpsModelOverviewView)modelOverviewView).showOverview((IType)element.getIpsObject());
                } catch (CoreException e) {
                    throw new CoreRuntimeException(e);
                }
            } else if (selection.getFirstElement() instanceof IpsProject) {
                ((IpsModelOverviewView)modelOverviewView).showOverview((IpsProject)firstElement);
            } else {
                throw new CoreRuntimeException("The selection must be of type IType or an IpsProject."); //$NON-NLS-1$
            }
        } catch (PartInitException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

        return null;
    }

    protected TypedSelection<IAdaptable> getSelectionFromSelectionProvider() {
        TypedSelection<IAdaptable> typedSelection;
        ISelectionService selectionService = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
                .getSelectionService();
        typedSelection = new TypedSelection<IAdaptable>(IAdaptable.class, selectionService.getSelection());
        return typedSelection;
    }

}
