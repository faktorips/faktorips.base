package org.faktorips.devtools.core.ui.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.IIpsElement;

public class SearchResultLabelProvider implements ILabelProvider {

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
		return false;
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
        if (element instanceof Object[]) {
            return ((IIpsElement)((Object[])element)[0]).getImage();
        }
        else {
            return ((IIpsElement)element).getImage();
        }
	}

    /**
     * Overridden
     */
	public String getText(Object element) {
        if (element instanceof Object[]) {
            return ((IIpsElement)((Object[])element)[0]).getName();
        }
        else {
            return ((IIpsElement)element).getName();
        }
	}

}
