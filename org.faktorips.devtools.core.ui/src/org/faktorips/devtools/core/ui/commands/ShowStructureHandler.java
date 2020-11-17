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
import org.faktorips.devtools.core.ui.views.productstructureexplorer.ProductStructureExplorer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;

/**
 * Handler for opening the StructureExplorer for a selected ProductCmpt. Other types of IpsObjects
 * are ignored.
 * 
 * @author Stefan Widmaier
 */
public class ShowStructureHandler extends IpsAbstractHandler {

    @Override
    public void execute(ExecutionEvent event, IWorkbenchPage activePage, IIpsSrcFile ipsSrcFile) {
        if (IpsObjectType.PRODUCT_CMPT.equals(ipsSrcFile.getIpsObjectType())) {

            try {
                IViewPart pse = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage()
                        .showView(ProductStructureExplorer.EXTENSION_ID, null, IWorkbenchPage.VIEW_ACTIVATE);
                ((ProductStructureExplorer)pse).showStructure(ipsSrcFile);
            } catch (PartInitException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }

    }
}
