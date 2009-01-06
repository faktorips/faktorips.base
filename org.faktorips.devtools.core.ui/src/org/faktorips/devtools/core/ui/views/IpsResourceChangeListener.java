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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;

public abstract class IpsResourceChangeListener implements IResourceChangeListener {

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
        IResource[] resources= internalResourceChanged(event);
    	
    	MyRunnable runnable = new MyRunnable(resources);
    	
    	if (viewer != null) {
    		Control ctrl = viewer.getControl();
    		if (ctrl != null && !ctrl.isDisposed()) {
    			ctrl.getDisplay().syncExec(runnable);
    		}
    	}
    }
    
    protected abstract IResource[] internalResourceChanged(IResourceChangeEvent event);

    /**
     * Refreshes the given IResource in the viewer and selects it afterwards.
     * 
     */
    private class MyRunnable implements Runnable {
    	IResource[] resources;
    	public MyRunnable(IResource[] resources) {
    		this.resources = resources;
    	}
    	
        public void run() {
        	if (viewer == null || viewer.getControl().isDisposed()) {
        		return;
        	}
        	viewer.refresh();
        	for (int i = 0; i < resources.length; i++) {
                IResource res= resources[i];
        		IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(res);
                // performs full refresh if element is null
                viewer.refresh(element);
                
        		if (element != null) {
        			viewer.setSelection(new StructuredSelection(element), true);
        		}
        	}
        }
    	
    }
}
