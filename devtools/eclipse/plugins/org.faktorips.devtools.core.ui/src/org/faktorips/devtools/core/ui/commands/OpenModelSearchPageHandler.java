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
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.plugin.IpsStatus;

public class OpenModelSearchPageHandler extends AbstractHandler {

    public static final String CONTRIBUTION_ID = "org.faktorips.devtools.core.ui.search.model"; //$NON-NLS-1$
    private static final String MODEL_SEARCH_PAGE_ID = "org.faktorips.devtools.core.ui.search.model.ModelSearchPage"; //$NON-NLS-1$

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        IWorkbenchWindow window = IpsUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();

        if (window == null || window.getActivePage() == null) {
            IpsPlugin.log(new IpsStatus("No window handle for opening the Faktor-IPS Model Search.")); //$NON-NLS-1$
            return null;
        }

        NewSearchUI.openSearchDialog(window, MODEL_SEARCH_PAGE_ID);
        return null;
    }
}
