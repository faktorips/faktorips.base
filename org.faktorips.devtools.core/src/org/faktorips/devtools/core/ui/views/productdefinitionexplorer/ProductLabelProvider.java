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

package org.faktorips.devtools.core.ui.views.productdefinitionexplorer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragment;

public class ProductLabelProvider implements ILabelProvider {

	private List listeners = new ArrayList();
	
    /**
     * Overridden
     */
	public void addListener(ILabelProviderListener listener) {
		listeners.add(listener);
	}
    
	/**
     * Overridden
	 */
	public void dispose() {
		this.listeners = null;
	}

    /**
     * Overridden
     */
	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

    /**
     * Overridden
     */
	public void removeListener(ILabelProviderListener listener) {
		listeners.remove(listener);
	}

    /**
     * Overridden
     */
	public Image getImage(Object element) {
		Image image = null;
        if (element instanceof IIpsPackageFragment) {
            image = IpsPlugin.getDefault().getImage("folder_open.gif"); //$NON-NLS-1$
        }
        else if (element instanceof IIpsElement) {
			image = ((IIpsElement)element).getImage();
		}
        
        return image;
	}

    /**
     * Overridden
     */
	public String getText(Object element) {
		String text = null;
        if (element instanceof IIpsPackageFragment) {
            text = ((IIpsPackageFragment)element).getFolderName();
        } else if (element instanceof IIpsElement) {
			text = ((IIpsElement)element).getName();
		}
		
		return text;
	}	
}
