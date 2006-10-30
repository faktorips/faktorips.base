package org.faktorips.devtools.core.ui.views.modelexplorer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;
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
		if(element instanceof IIpsElement){
			/*if (element instanceof IIpsPackageFragment) {
				return IpsPlugin.getDefault().getImage("folder_open.gif"); //$NON-NLS-1$
			} else{*/
				return ((IIpsElement) element).getImage();
			//}
		}else if(element instanceof IResource){
	        IWorkbenchAdapter adapter= null;
	        if (element instanceof IAdaptable) {
	            adapter= (IWorkbenchAdapter) ((IAdaptable) element).getAdapter(IWorkbenchAdapter.class);
	        }
	        if (adapter == null) {
	            return null;
	        }
	        ImageDescriptor descriptor = adapter.getImageDescriptor(element);
	        if (descriptor == null) {
	            return null;
	        }
	        return descriptor.createImage();
		}
		return null;
	}
	
	public String getText(Object element) {
		if(element instanceof IIpsElement){
			if (element instanceof IIpsPackageFragment) {
				IIpsPackageFragment fragment= (IIpsPackageFragment) element;
				if(fragment.isDefaultPackage()){
					return Messages.ModelExplorer_defaultPackageLabel;
				}
				if (!isFlatLayout) {
					return fragment.getFolderName();
				} else {
					return fragment.getName();
				}
				
			} else if (element instanceof IAttribute) {
				IAttribute attrib = (IAttribute) element;
				StringBuffer sb= new StringBuffer();
				sb.append(attrib.getName());
				sb.append(" : "); //$NON-NLS-1$
				sb.append(attrib.getDatatype());
				sb.append(", "); //$NON-NLS-1$
				sb.append(attrib.getAttributeType().getId());
				return sb.toString();
			}
			return ((IIpsElement) element).getName();
		}else{
			if(element instanceof IProject){
				if(((IProject)element).isOpen()){
					return ((IProject)element).getName()+" ("+Messages.ModelExplorer_nonIpsProjectLabel+")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
			return ((IResource)element).getName();
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
