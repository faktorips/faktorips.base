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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.ui.search.ReferencesToProductSearchQuery;
import org.faktorips.devtools.core.ui.views.productdefinitionexplorer.ProductExplorer;

/**
 * Find all product components which refer to the selected one.
 * 
 * @author Thorsten Guenther
 */
public class FindReferenceAction extends Action {

    public FindReferenceAction() {
        super();
        this.setDescription(Messages.FindReferenceAction_description);
        this.setText(Messages.FindReferenceAction_name);
        this.setToolTipText(this.getDescription());
    }
    
    public void run() {
        try {
            IWorkbench wb = IpsPlugin.getDefault().getWorkbench();
            IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();
            IViewReference[] views = page.getViewReferences();
    
            IViewPart pe = null;
            for (int i = 0; i < views.length; i++) {
                if (views[i].getId().equals(ProductExplorer.EXTENSION_ID)) {
                    pe = views[i].getView(true);
                    break;
                }
            }
            
            if (pe == null) {
                pe = IpsPlugin.getDefault().getWorkbench().getViewRegistry().find(ProductExplorer.EXTENSION_ID).createView();
            }
    
            IProductCmpt referenced = ((ProductExplorer)pe).getSelectedProductCmpt();
    
            if (referenced != null) {
                NewSearchUI.activateSearchResultView();
                NewSearchUI.runQueryInBackground(new ReferencesToProductSearchQuery(referenced));
            }
        } catch (PartInitException e) {
            IpsPlugin.log(e);
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }
    
}
