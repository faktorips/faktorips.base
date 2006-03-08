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
