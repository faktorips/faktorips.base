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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.core.ui.views.modeloverview.ModelOverview;

public class ShowModelOverviewHandler extends IpsAbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IIpsSrcFile ipsSrcFile = getCurrentlySelectedIpsSrcFile();

        IWorkbenchWindow activeWindow = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage activePage = activeWindow.getActivePage();
        execute(event, activePage, ipsSrcFile);

        // return must be null - see jdoc
        return null;
    }

    @Override
    public void execute(ExecutionEvent event, IWorkbenchPage activePage, IIpsSrcFile ipsSrcFile)
            throws ExecutionException {
        // this method must be called first! Otherwise the selection is not valid.
        TypedSelection<IAdaptable> selection = new TypedSelection<IAdaptable>(IAdaptable.class,
                HandlerUtil.getCurrentSelection(event));

        try {
            IViewPart modelOverviewView = activePage.showView(ModelOverview.EXTENSION_ID, null,
                    IWorkbenchPage.VIEW_ACTIVATE);
            IAdaptable firstElement = selection.getFirstElement();
            if (firstElement instanceof IpsProject) {
                ((ModelOverview)modelOverviewView).showOverview((IpsProject)firstElement);
            } else if (ipsSrcFile != null) {
                try {
                    ((ModelOverview)modelOverviewView).showOverview((IType)ipsSrcFile.getIpsObject());
                } catch (CoreException e) {
                    throw new CoreRuntimeException(e);
                }
            } else {
                throw new CoreRuntimeException(
                        "The selection must be of type IType, IpsProject or ComponentNode, but was " + selection.getFirstElement().getClass()); //$NON-NLS-1$
            }
        } catch (PartInitException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

    }

}
