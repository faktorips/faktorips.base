package org.faktorips.devtools.core.ui.views;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;

public class IpsElementDragListener implements DragSourceListener {

    StructuredViewer dragSource;
    
    public IpsElementDragListener(StructuredViewer dragSource) {
        this.dragSource = dragSource;
    }
    
    public void dragStart(DragSourceEvent event) {
        event.doit = getFilenames((IStructuredSelection)dragSource.getSelection()).length > 0;
    }

    public void dragSetData(DragSourceEvent event) {
    	event.data = getFilenames((IStructuredSelection)dragSource.getSelection());    	
    }

    public void dragFinished(DragSourceEvent event) {
        // nothing to do
    }

    private String[] getFilenames(IStructuredSelection selection) {
    	ArrayList list = new ArrayList(selection.size());
    	Iterator iter = ((IStructuredSelection)dragSource.getSelection()).iterator();
    	while (iter.hasNext()) {
    		Object selected = iter.next();
    		if (selected instanceof IIpsElement && ((IIpsElement)selected).getCorrespondingResource() != null) {
    			list.add(((IIpsElement)selected).getCorrespondingResource().getLocation().toOSString());
    		}
    		else if (selected instanceof IProductCmpt) {
    			list.add(((IProductCmpt)selected).getIpsSrcFile().getCorrespondingFile().getLocation().toOSString());
    		}
    	}
    	
    	return (String[])list.toArray(new String[list.size()]);
    }
}
