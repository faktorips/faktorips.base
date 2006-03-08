package org.faktorips.devtools.core.ui.actions;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;


public class IpsDeleteAction extends IpsAction {

    public IpsDeleteAction(ISelectionProvider selectionProvider) {
        super(selectionProvider);
    }

    public void run(IStructuredSelection selection) {
        List selectedObjects = selection.toList();

        for (Iterator iter = selectedObjects.iterator(); iter.hasNext();) {
            Object selected = iter.next();

            if (selected instanceof IIpsObjectPart) {
                ((IIpsObjectPart)selected).delete();
            } else if (selected instanceof IIpsElement) {
                IResource res;
                if (selected instanceof IProductCmpt || selected instanceof ITableContents) {
                    res = ((IIpsObject)selected).getEnclosingResource();
                } else {
                    res = ((IIpsElement)selected).getCorrespondingResource();
                }
                if (res != null) {
                    try {
                        res.delete(true, null);
                    } catch (CoreException e) {
                        IpsPlugin.logAndShowErrorDialog(e);
                    }
                }
            }
        }
    }
}
