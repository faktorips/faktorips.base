package org.faktorips.devtools.core.ui.views.modelexplorer;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.pctype.IAttribute;

/**
 * This class provides the ModelExplorer with labels for its tree-elements.
 * Label names for PackageFragments are dependant on the current layout style
 * indicated by the <code>isFlatLayout</code> flag.
 * 
 * @author Stefan Widmaier
 */
public class ModelLabelProvider implements ILabelProvider {
	private boolean isFlatLayout = false;
	
	public ModelLabelProvider(){
		super();
	}
	public ModelLabelProvider(boolean flatLayout){
		isFlatLayout= flatLayout;
	}

	public Image getImage(Object element) {
		Image img = null;
		if (element instanceof IIpsPackageFragment) {
			img = IpsPlugin.getDefault().getImage("folder_open.gif"); //$NON-NLS-1$
		} else if (element instanceof IIpsElement) {
			img = ((IIpsElement) element).getImage();
		}
		return img;
	}

	public String getText(Object element) {
		if (element instanceof IIpsPackageFragment) {
			if (!isFlatLayout) {
				return ((IIpsPackageFragment) element).getFolderName();
			} else {
				return ((IIpsElement) element).getName();
			}
		} else if (element instanceof IAttribute) {
			IAttribute attrib = (IAttribute) element;
			StringBuffer sb= new StringBuffer();
			sb.append(attrib.getName());
			sb.append("     ["); //$NON-NLS-1$
			sb.append(attrib.getDatatype());
			sb.append(", "); //$NON-NLS-1$
			sb.append(attrib.getAttributeType().getId());
			sb.append("]"); //$NON-NLS-1$
			return sb.toString();
		} else {
			return ((IIpsElement) element).getName();
		}
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	public void removeListener(ILabelProviderListener listener) {
	}

	/**
	 * Sets the flag for flat respectivly hierarchical labelnames
	 * 
	 * @param b
	 */
	/* package */ void setIsFlatLayout(boolean b) {
		isFlatLayout = b;
	}

}
