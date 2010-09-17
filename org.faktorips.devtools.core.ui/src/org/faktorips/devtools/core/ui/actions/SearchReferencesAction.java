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
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.search.ReferencesToPolicySearchQuery;
import org.faktorips.devtools.core.ui.search.ReferencesToProductSearchQuery;
import org.faktorips.devtools.core.ui.search.ReferencesToTableContentsSearchQuery;

/**
 * Action for finding references to a given IpsObject. Actually supported are {@link IProductCmpt},
 * {@link ITableContents} and {@link IPolicyCmptType}
 * 
 * @author Thorsten Guenther
 * @author Stefan Widmaier
 * @author dirmeier
 */
public class SearchReferencesAction extends IpsAction {

    /**
     * Creates a new action to find references to a product component. The product componente to
     * find the references to is queried from the given selection provider.
     * <p>
     * Note: Only <code>IStructuredSelection</code>s are supported.
     */
    public SearchReferencesAction(ISelectionProvider selectionProvider) {
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
            } else if (selected instanceof IPolicyCmptType) {
                IPolicyCmptType referenced = (IPolicyCmptType)selected;
                NewSearchUI.activateSearchResultView();
                NewSearchUI.runQueryInBackground(new ReferencesToPolicySearchQuery(referenced));
            }
        }
    }

    @Override
    protected boolean computeEnabledProperty(IStructuredSelection selection) {
        IIpsObject[] ipsObjects = getIpsObjectsForSelection(selection);
        if (ipsObjects.length != 1) {
            return false;
        }
        IIpsObject selected = ipsObjects[0];
        return selected instanceof IProductCmpt || selected instanceof ITableContents
                || selected instanceof IPolicyCmptType;
    }

}
