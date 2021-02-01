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
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IWorkbenchPage;
import org.faktorips.devtools.core.ui.search.reference.ReferencesToIpsObjectSearchQuery;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.tablecontents.ITableContents;

/**
 * Handler for finding references to a given IpsObject. Actually supported are {@link IProductCmpt},
 * {@link ITableContents} and {@link IPolicyCmptType}
 * 
 */
public class SearchReferencesHandler extends IpsAbstractHandler {

    @Override
    public void execute(ExecutionEvent event, IWorkbenchPage activePage, IIpsSrcFile ipsSrcFile) {
        IIpsObject selected;
        selected = ipsSrcFile.getIpsObject();
        if (selected != null) {
            NewSearchUI.activateSearchResultView();
            NewSearchUI.runQueryInBackground(new ReferencesToIpsObjectSearchQuery(selected));
        }
    }

}
