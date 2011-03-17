/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
import org.faktorips.devtools.core.ui.search.ReferencesToPolicySearchQuery;
import org.faktorips.devtools.core.ui.search.ReferencesToProductSearchQuery;
import org.faktorips.devtools.core.ui.search.ReferencesToTableContentsSearchQuery;

/**
 * Handler for finding references to a given IpsObject. Actually supported are {@link IProductCmpt},
 * {@link ITableContents} and {@link IPolicyCmptType}
 * 
 * @author Thorsten Guenther
 * @author Stefan Widmaier
 * @author dirmeier
 * @author stoll
 */
public class SearchReferencesHandler extends IpsAbstractHandler {

    @Override
    public void execute(ExecutionEvent event, IWorkbenchPage activePage, IIpsSrcFile ipsSrcFile) {
        IIpsObject selected;
        try {
            selected = ipsSrcFile.getIpsObject();

            if (selected != null) {
                if (selected instanceof IProductCmpt) {
                    NewSearchUI.activateSearchResultView();
                    NewSearchUI.runQueryInBackground(new ReferencesToProductSearchQuery((IProductCmpt)selected));
                } else if (selected instanceof ITableContents) {
                    NewSearchUI.activateSearchResultView();
                    NewSearchUI
                            .runQueryInBackground(new ReferencesToTableContentsSearchQuery((ITableContents)selected));
                } else if (selected instanceof IPolicyCmptType) {
                    IPolicyCmptType referenced = (IPolicyCmptType)selected;
                    NewSearchUI.activateSearchResultView();
                    NewSearchUI.runQueryInBackground(new ReferencesToPolicySearchQuery(referenced));
                }
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

    }

}
