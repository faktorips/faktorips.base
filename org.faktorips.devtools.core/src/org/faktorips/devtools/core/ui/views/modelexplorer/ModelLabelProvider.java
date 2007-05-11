package org.faktorips.devtools.core.ui.views.modelexplorer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
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
		    return ((IIpsElement) element).getImage();
		} else if(element instanceof IResource){
            // check if the resource is an ips source file, in this case return the image of the ips source,
            // remark: if we use the IWorkbenchAdapter to retrieve the image, we get the
            // standard icon of the ips object (resolved by the filename and defined in the extension point)
            // - but the element is no valid ips object (e.g. not inside an ips package) -
            // therefore to differ between "valid" and "invalid" ips objects in the model explorer we return 
            // a different icon
            IIpsElement ipsElement = IpsPlugin.getDefault().getIpsModel().getIpsElement((IResource)element);
            if (ipsElement != null && ipsElement instanceof IIpsSrcFile){
                return IpsObjectType.IPS_SOURCE_FILE.getEnabledImage();
            }
            
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
		if (element instanceof IIpsElement) {
            if (element instanceof IIpsPackageFragment) {
                IIpsPackageFragment fragment = (IIpsPackageFragment)element;
                if (fragment.isDefaultPackage()) {
                    return Messages.ModelExplorer_defaultPackageLabel;
                }
                if (isFlatLayout) {
                    return fragment.getName();
                } else {
                    String name = fragment.getName();
                    int index = name.lastIndexOf('.');
                    if (index == -1) {
                        return name;
                    }
                    return name.substring(index + 1);
                }
            } else if (element instanceof IAttribute) {
                IAttribute attrib = (IAttribute)element;
                StringBuffer sb = new StringBuffer();
                sb.append(attrib.getName());
                sb.append(" : "); //$NON-NLS-1$
                sb.append(attrib.getDatatype());
                sb.append(", "); //$NON-NLS-1$
                sb.append(attrib.getAttributeType().getId());
                return sb.toString();
            } else if (element instanceof IIpsProject && !((IIpsProject)element).isProductDefinitionProject()) {
                return ((IIpsProject)element).getName()
                        + NLS.bind(" ({0})", Messages.ModelLabelProvider_LabelNoProductDefinitionProject); //$NON-NLS-1$
            }
            return ((IIpsElement)element).getName();
        } else if (element instanceof IProject && ((IProject)element).isOpen()) {
            return ((IProject)element).getName()
                    + NLS.bind(" ({0})", Messages.ModelLabelProvider_LabelNoProductDefinitionProject); //$NON-NLS-1$
        } else {
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
