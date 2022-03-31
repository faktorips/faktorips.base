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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.core.ui.views.modelstructure.ModelStructure;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.type.IType;

public class ShowModelStructureHandler extends IpsAbstractHandler {

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
        TypedSelection<IAdaptable> selection = new TypedSelection<>(IAdaptable.class,
                HandlerUtil.getCurrentSelection(event));
        try {
            IViewPart modelOverviewView = activePage.showView(ModelStructure.EXTENSION_ID, null,
                    IWorkbenchPage.VIEW_ACTIVATE);
            IAdaptable firstElement = selection.getFirstElement();
            if (firstElement instanceof IpsProject) {
                ((ModelStructure)modelOverviewView).showStructure((IpsProject)firstElement);
            } else if (ipsSrcFile != null) {
                ((ModelStructure)modelOverviewView).showStructure((IType)ipsSrcFile.getIpsObject());
            } else {
                throw new IpsException(
                        "The selection must be of type IType, IpsProject or ComponentNode, but was " //$NON-NLS-1$
                                + selection.getFirstElement().getClass());
            }
        } catch (PartInitException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

    }

}
