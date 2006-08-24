/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views;

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;

/**
 * Abstract default implementation of a drop target listener. Drag over and drag leave
 * are ignored by this implementation, dargOperationChanged too.  
 * 
 * @author Thorsten Guenther
 */
public abstract class IpsElementDropListener implements DropTargetListener {

	/**
	 * Empty default implementation.
	 * 
	 * {@inheritDoc}
	 */
	public void dragLeave(DropTargetEvent event) {
		// nothing done as default
	}

	/**
	 * Empty default implementation.
	 * 
	 * {@inheritDoc}
	 */
	public void dragOperationChanged(DropTargetEvent event) {
		// nothing done as default
	}

	/**
	 * Empty default implementation.
	 * 
	 * {@inheritDoc}
	 */
	public void dragOver(DropTargetEvent event) {
		// nothing done as default
	}

	/**
	 * Returns all <code>IIpsElement</code>s transferred as files by the given transferData
	 * object as array.
	 */
	protected IIpsElement[] getTransferedElements(TransferData transferData) {
		String[] filenames = (String[])FileTransfer.getInstance().nativeToJava(transferData);
		ArrayList elements = new ArrayList();
		for (int i = 0; i < filenames.length; i++) {
			Path path = new Path(filenames[i]);
			
            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
            IContainer container = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(path);
            if(file!=null){
                // getFileForLocation returns a file even if the path points to a folder.
                // In this case file.exists() returns false.s
                if(file.exists()){
                    IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(file);
                    if (element != null && element.exists()) {
                        elements.add(element);
                    }
                }
            }
            else if(container!=null){ 
                if(container.exists()){
                    IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(container);
                    if (element != null && element.exists()) {
                        elements.add(element);
                    }
                }
            }
		}
		return (IIpsElement[])elements.toArray(new IIpsElement[elements.size()]);
	}
	
}
