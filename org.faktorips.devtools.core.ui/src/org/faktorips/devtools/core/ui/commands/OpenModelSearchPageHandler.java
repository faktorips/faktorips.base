/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class OpenModelSearchPageHandler extends AbstractHandler {

    private static final String MODEL_SEARCH_PAGE_ID = "org.faktorips.devtools.core.ui.search.model.ModelSearchPage"; //$NON-NLS-1$
    public static final String CONTRIBUTION_ID = "org.faktorips.devtools.core.ui.search.model"; //$NON-NLS-1$

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
