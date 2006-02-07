package org.faktorips.devtools.core.ui.views.attrtable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.ui.views.productdefinitionexplorer.ProductExplorer;

public class ShowAttributesActionDelegate implements IWorkbenchWindowActionDelegate {

    public void dispose() {
        // nothing to do
    }

    public void init(IWorkbenchWindow window) {
        // nothing to do
    }

    public void run(IAction action) {
        try {
            IViewReference[] views = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
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
            IProductCmpt selected = ((ProductExplorer)pe).getSelectedProductCmpt();
            
            if (selected != null) {
                IViewPart attrTable = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(AttributesTable.EXTENSION_ID);
                ((AttributesTable)attrTable).show(selected.findPolicyCmptType());
            }
        } catch (PartInitException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // nothing to do
    }

}
