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
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.views.ipshierarchy.IpsHierarchyView;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * ShowHierarchyHandler is a defaultHandler for the command id:
 * org.faktorips.devtools.core.ui.actions.showHierarchy in plugin.xml Extensions
 * org.eclipse.ui.commands Opens or updates IpsHierarchyView
 * 
 * @author stoll
 */
public class ShowHierarchyHandler extends IpsAbstractHandler {

    @Override
    public void execute(ExecutionEvent event, IWorkbenchPage activePage, IIpsSrcFile ipsSrcFile) {
        IIpsObject ipsObject = ipsSrcFile.getIpsObject();
        if (IpsHierarchyView.supports(ipsObject)) {
            try {
                IViewPart hierarchyView = activePage.showView(IpsHierarchyView.EXTENSION_ID, null,
                        IWorkbenchPage.VIEW_ACTIVATE);
                ((IpsHierarchyView)hierarchyView).showHierarchy(ipsObject);
            } catch (PartInitException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
    }
}
