package org.faktorips.devtools.core.ui.views;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.DummyRoot;

public class ProductCmptDragListener implements DragSourceListener {

    StructuredViewer dragSource;
    
    public ProductCmptDragListener(StructuredViewer dragSource) {
        this.dragSource = dragSource;
    }
    
    public void dragStart(DragSourceEvent event) {
        Object selected = ((IStructuredSelection)dragSource.getSelection()).getFirstElement();
        if (selected instanceof IProductCmpt || selected instanceof DummyRoot) {
            event.doit = true;
        }
        else {
            event.doit = false;
        }
        
    }

    public void dragSetData(DragSourceEvent event) {
        Object selected = ((IStructuredSelection)dragSource.getSelection()).getFirstElement();
        if (selected instanceof IProductCmpt) {
            event.data = ((IProductCmpt)selected).getQualifiedName();
        }
        else if (selected instanceof DummyRoot) {
            event.data = ((DummyRoot)selected).data.getQualifiedName();
        }
    }

    public void dragFinished(DragSourceEvent event) {
        // nothing to do
    }
    
}
