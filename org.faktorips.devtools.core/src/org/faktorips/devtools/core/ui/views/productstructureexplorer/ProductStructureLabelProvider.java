package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;

public class ProductStructureLabelProvider implements ILabelProvider {

	private List listeners = new ArrayList();
	
	/**
	 * {@inheritDoc}
	 */
	public void addListener(ILabelProviderListener listener) {
		listeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
		this.listeners = null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isLabelProperty(Object element, String property) {
		return true; 
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeListener(ILabelProviderListener listener) {
		listeners.remove(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public Image getImage(Object element) {
        if (element instanceof Node) {
            return ((Node)element).getImage();
        } 
		else {
		    return IpsPlugin.getDefault().getImage(Messages.ProductStructureLabelProvider_undefined);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getText(Object element) {
        if (element instanceof Node) {
            return ((Node)element).getText();
        } 
		else {
		    return Messages.ProductStructureLabelProvider_undefined;
		}
	}

}
