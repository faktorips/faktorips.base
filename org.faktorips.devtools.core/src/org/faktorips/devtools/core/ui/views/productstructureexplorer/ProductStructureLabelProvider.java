package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;

public class ProductStructureLabelProvider implements ILabelProvider {

	private List listeners = new ArrayList();
	
	public void addListener(ILabelProviderListener listener) {
		listeners.add(listener);
	}

	public void dispose() {
		this.listeners = null;
	}

	public boolean isLabelProperty(Object element, String property) {
		return true; 
	}

	public void removeListener(ILabelProviderListener listener) {
		listeners.remove(listener);
	}

	public Image getImage(Object element) {
        if (element instanceof DummyRoot) {
            return ((DummyRoot)element).data.getImage();
        } 
        else if (element instanceof IIpsElement) {
			return ((IIpsElement)element).getImage();
		}
		else {
		    return IpsPlugin.getDefault().getImage(Messages.ProductStructureLabelProvider_undefined);
		}
	}

	public String getText(Object element) {
        if (element instanceof DummyRoot) {
            return ((DummyRoot)element).data.getName();
        } 
        else if (element instanceof IIpsElement) {
			return ((IIpsElement)element).getName();
		}
		else {
		    return Messages.ProductStructureLabelProvider_undefined;
		}
	}

}
