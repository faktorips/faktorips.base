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
        if (element instanceof IIpsPackageFragment) {
            return IpsPlugin.getDefault().getImage("folder_open.gif"); //$NON-NLS-1$
        }
        else if (element instanceof IIpsElement) {
			return ((IIpsElement)element).getImage();
		}
		else {
		    return null;
		}
	}

    /**
     * Overridden
     */
	public String getText(Object element) {
        if (element instanceof IIpsPackageFragment) {
            return ((IIpsPackageFragment)element).getFolderName();
        }
		if (element instanceof IIpsElement) {
			return ((IIpsElement)element).getName();
		}
		else {
		    return null;
		}
	}

}
