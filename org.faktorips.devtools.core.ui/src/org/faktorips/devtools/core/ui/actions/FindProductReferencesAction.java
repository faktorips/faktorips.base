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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.NewSearchUI;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.search.ReferencesToProductSearchQuery;
import org.faktorips.devtools.core.ui.search.ReferencesToTableContentsSearchQuery;

/**
 * Find all product components which refer to the selected one.
 * 
 * @author Thorsten Guenther
 * @author Stefan Widmaier
 */
public class FindProductReferencesAction extends IpsAction {

    /**
     * Creates a new action to find references to a product component. The product componente to
     * find the references to is queried from the given selection provider.
     * <p>
     * Note: Only <code>IStructuredSelection</code>s are supported.
     */
    public FindProductReferencesAction(ISelectionProvider selectionProvider) {
        super(selectionProvider);
        this.setDescription(Messages.FindProductReferencesAction_description);
        this.setText(Messages.FindProductReferencesAction_name);
        this.setToolTipText(this.getDescription());
    }

    @Override
    public void run(IStructuredSelection selection) {
        Object selected = getIpsObjectForSelection(selection);
        if (selected != null) {
            if (selected instanceof IProductCmpt) {
                NewSearchUI.activateSearchResultView();
                NewSearchUI.runQueryInBackground(new ReferencesToProductSearchQuery((IProductCmpt)selected));
            } else if (selected instanceof ITableContents) {
                NewSearchUI.activateSearchResultView();
                NewSearchUI.runQueryInBackground(new ReferencesToTableContentsSearchQuery((ITableContents)selected));
            }
        }
    }

}
