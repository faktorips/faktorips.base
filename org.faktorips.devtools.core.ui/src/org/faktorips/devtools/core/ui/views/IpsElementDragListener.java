/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.faktorips.devtools.core.model.IIpsElement;

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

    public static String[] getFilenames(IStructuredSelection selection) {
    	ArrayList list = new ArrayList(selection.size());
    	Iterator iter =  selection.iterator();
    	while (iter.hasNext()) {
    		Object selected = iter.next();
    		if(selected instanceof Object[]){
                Object[] objetcs = (Object[])selected ;
    			for (int i = 0; i < objetcs.length; i++) {
                    addSelectedObject(list, objetcs[i]);
                }
    		} else {
                addSelectedObject(list, selected);
            }
    	}
    	
    	return (String[])list.toArray(new String[list.size()]);
    }
    
    private static void addSelectedObject(List list, Object selected){
        if(selected instanceof IResource){
            list.add(((IResource)selected).getLocation().toOSString());
        } else if (selected instanceof IIpsElement){
            if (((IIpsElement)selected).getCorrespondingResource() != null) {
                list.add(((IIpsElement)selected).getCorrespondingResource().getLocation().toOSString());
            }
        }
    }
}
