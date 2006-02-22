package org.faktorips.devtools.core.ui.views;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IpsObjectType;

public class ProductCmptDropListener implements DropTargetListener {

    StructuredViewer dropTarget;
    
    public ProductCmptDropListener(StructuredViewer dropTarget) {
        this.dropTarget = dropTarget;
    }
    
    public void dragEnter(DropTargetEvent event) {
        event.detail = DND.DROP_LINK;
    }

    public void dragLeave(DropTargetEvent event) {
        // nothing to do
    }

    public void dragOperationChanged(DropTargetEvent event) {
        // nothing to do
    }

    public void dragOver(DropTargetEvent event) {
        // nothing to do
    }

    public void drop(DropTargetEvent event) {
        try {
            dropTarget.setInput(IpsPlugin.getDefault().getIpsModel().getIpsProjects()[0].findIpsObject(IpsObjectType.PRODUCT_CMPT, (String)event.data));
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

    public void dropAccept(DropTargetEvent event) {
        event.detail = DND.DROP_LINK;
        
    }
}
