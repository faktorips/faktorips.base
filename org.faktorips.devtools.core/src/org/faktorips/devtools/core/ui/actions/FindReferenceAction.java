/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.NewSearchUI;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.ui.search.ReferencesToProductSearchQuery;

/**
 * Find all product components which refer to the selected one.
 * 
 * @author Thorsten Guenther
 */
public class FindReferenceAction extends Action {
	/**
	 * The selection provider to get the selection from if requested to run.
	 */
	private ISelectionProvider selectionProvider;
	
	/**
	 * Creates a new action to find references to a product component. The 
	 * product componente to find the references to is queried from the given
	 * selection provider.
	 * <p>
	 * Note: Only <code>IStructuredSelection</code>s are supported.
	 */
    public FindReferenceAction(ISelectionProvider selectionProvider) {
        super();
        this.selectionProvider = selectionProvider;
        this.setDescription(Messages.FindReferenceAction_description);
        this.setText(Messages.FindReferenceAction_name);
        this.setToolTipText(this.getDescription());
    }
    
    public void run() {
        	ISelection sel = selectionProvider.getSelection();

		if (!(sel instanceof IStructuredSelection)) {
			// we dont support simple selection
			return;
		}

		Object selected = ((IStructuredSelection) sel).getFirstElement();

		if (!(selected instanceof IProductCmpt)) {
			// we only support product components
			return;
		}

		IProductCmpt referenced = (IProductCmpt) selected;

		if (referenced != null) {
			NewSearchUI.activateSearchResultView();
			NewSearchUI
					.runQueryInBackground(new ReferencesToProductSearchQuery(
							referenced));
		}
    }
    
}
