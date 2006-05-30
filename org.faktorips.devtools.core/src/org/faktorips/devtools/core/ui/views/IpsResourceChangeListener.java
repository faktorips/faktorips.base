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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;

public class IpsResourceChangeListener implements IResourceChangeListener {

    /**
     * The viewer to update.
     */
    private StructuredViewer viewer;
    
    /**
     * Creates a new ResourceChangeListener which will update the given StructuredViewer 
     * if an event occurs.
     * 
     * @param viewer
     */
    public IpsResourceChangeListener(StructuredViewer viewer) {
        this.viewer = viewer;
    }
    
    /**
     * Overridden.
     */
    public void resourceChanged(IResourceChangeEvent event) {
    	if (event.getBuildKind() != 0) {
    		return;
    	}
    	
    	IResource res = null;
    	for (IResourceDelta deltas[] = event.getDelta().getAffectedChildren(); deltas.length > 0; deltas = deltas[0].getAffectedChildren()) {
    		res = deltas[0].getResource();
    		if (res instanceof IFolder && IResourceDelta.ADDED == deltas[0].getKind()) {
    			break;
    		}
    	}
    	
    	MyRunnable runnable = new MyRunnable(res);
    	
    	if (viewer != null) {
    		Control ctrl = viewer.getControl();
    		if (ctrl != null && !ctrl.isDisposed()) {
    			ctrl.getDisplay().syncExec(runnable);
    		}
    	}
    	
     	
    }
    
    private class MyRunnable implements Runnable {
    	IResource res;
    	public MyRunnable(IResource res) {
    		this.res = res;
    	}
    	
        public void run() {
        	if (viewer == null || viewer.getControl().isDisposed()) {
        		return;
        	}
        	viewer.refresh();
        	if (res != null) {
        		IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(res);
        		if (element != null) {
        			viewer.setSelection(new StructuredSelection(element), true);
        		}
        	}
        }
    	
    }
}
