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
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.views.instanceexplorer.InstanceExplorer;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * An Handler to show a selected element in instance explorer
 * 
 * @author Cornelius Dirmeier
 */
public class ShowInstanceHandler extends IpsAbstractHandler {

    @Override
    public void execute(ExecutionEvent event, IWorkbenchPage activePage, IIpsSrcFile ipsSrcFile) {
        try {
            IIpsObject ipsObject = ipsSrcFile.getIpsObject();

            if (ipsObject == null) {
                return;
            }
            if (InstanceExplorer.supports(ipsObject)) {
                try {
                    IViewPart instanceExplorer = activePage.showView(InstanceExplorer.EXTENSION_ID, null,
                            IWorkbenchPage.VIEW_ACTIVATE);
                    ((InstanceExplorer)instanceExplorer).showInstancesOf(ipsObject);
                } catch (PartInitException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        } catch (IpsException e1) {
            IpsPlugin.log(e1);
        }

    }

}
