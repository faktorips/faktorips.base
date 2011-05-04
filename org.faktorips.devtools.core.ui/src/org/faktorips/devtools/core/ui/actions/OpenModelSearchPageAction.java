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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;

public class OpenModelSearchPageAction implements IWorkbenchWindowActionDelegate {

    private static final String MODEL_SEARCH_PAGE_ID = "org.faktorips.devtools.core.ui.search.model.ModelSearchPage"; //$NON-NLS-1$

    private IWorkbenchWindow window;

    @Override
    public void run(IAction action) {
        if (window == null || window.getActivePage() == null) {
            IpsPlugin.log(new IpsStatus("No window handle for opening the Faktor-IPS Model Search.")); //$NON-NLS-1$
            return;
        }
        NewSearchUI.openSearchDialog(window, MODEL_SEARCH_PAGE_ID);
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        // nothing to do
    }

    @Override
    public void dispose() {
        window = null;
    }

    @Override
    public void init(IWorkbenchWindow window) {
        this.window = window;

    }

}
