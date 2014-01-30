/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IWorkbenchPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.search.reference.ReferencesToIpsObjectSearchQuery;

/**
 * Handler for finding references to a given IpsObject. Actually supported are {@link IProductCmpt},
 * {@link ITableContents} and {@link IPolicyCmptType}
 * 
 */
public class SearchReferencesHandler extends IpsAbstractHandler {

    @Override
    public void execute(ExecutionEvent event, IWorkbenchPage activePage, IIpsSrcFile ipsSrcFile) {
        IIpsObject selected;
        try {
            selected = ipsSrcFile.getIpsObject();
            if (selected != null) {
                NewSearchUI.activateSearchResultView();
                NewSearchUI.runQueryInBackground(new ReferencesToIpsObjectSearchQuery(selected));
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

    }

}
