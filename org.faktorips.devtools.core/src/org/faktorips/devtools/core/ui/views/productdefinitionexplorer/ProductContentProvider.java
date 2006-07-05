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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.ITimedIpsObject;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;

/**
 * Provides the elements of the FaktorIps-Model for the department.
 * 
 * @author Thorsten Guenther
 */
public class ProductContentProvider implements ITreeContentProvider {

    /**
     * Overridden.
     */
    public Object[] getChildren(Object parentElement) {
        // For the department, we only descend up to the product into the model.
        if (!(parentElement instanceof IIpsElement) || parentElement instanceof IProductCmpt) {
            return new Object[0];
        }
        try {
            // For the department, we don't show IpsPackageFragmentRoots (children of IpsProject)
            if (parentElement instanceof IIpsProject) {
                
                IIpsElement[] children = ((IIpsElement)parentElement).getChildren();
                if (children.length >= 1) {
                    if (children.length > 1) {
                        IpsPlugin
                                .log(new IpsStatus(
                                        Messages.ProductContentProvider_tooMuchIpsPackageFragmentRoots));
                    }
                    return getPackageFragmentContent(((IIpsPackageFragmentRoot)children[0]).getIpsDefaultPackageFragment());
                    
                } else {
                    return new Object[0];
                }
            } else if (parentElement instanceof IIpsPackageFragment) {
                return getPackageFragmentContent((IIpsPackageFragment)parentElement);
            } else {
                return ((IIpsElement)parentElement).getChildren();
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return new Object[0];
        }
    }

    /**
     * Returns the relevant content (Package Fragments, Table Content or Products) contained in the given
     * Package Fragement. Contained package fragements are not processed.
     * 
     * @param parentElement The Package Fragment to search.
     * @return see above.
     * @throws CoreException
     */
    private Object[] getPackageFragmentContent(IIpsPackageFragment parentElement) throws CoreException {     
    	
    	if (!parentElement.getCorrespondingResource().isSynchronized(IResource.DEPTH_ZERO)) {
    		parentElement.getCorrespondingResource().refreshLocal(IResource.DEPTH_ZERO, null);
    	}
    	
        // For the department, we don't show IpsObjects or IpsSrcFiles, only IpsProductComponent(children of IpsSrcFile)
        // and IpsObjects (children of IpsSrcFile).                    
        IIpsElement folders[] = parentElement.getIpsChildPackageFragments();
        Object files[] = parentElement.getChildren();
        
        ArrayList result = new ArrayList();
        
        for (int i = 0; i < files.length; i++) {
            if (files[i] instanceof IIpsSrcFile) {
            	IFile file = ((IIpsSrcFile)files[i]).getCorrespondingFile();
            	if (!file.isSynchronized(IResource.DEPTH_ZERO)) {
            		file.getParent().refreshLocal(IResource.DEPTH_ONE, null);
            	}
                IIpsElement[] ipsObjects = ((IIpsSrcFile)files[i]).getChildren();
                if (ipsObjects.length > 1) {
                    IpsPlugin.log(new IpsStatus(Messages.ProductContentProvider_tooMuchIpsObjects));
                }

                if (ipsObjects.length >= 1 && (ipsObjects[0] instanceof IProductCmpt || ipsObjects[0] instanceof ITableContents)) {
                    result.add(ipsObjects[0]);
                } 
            } 
        }

        files = result.toArray();

        IIpsElement all[] = new IIpsElement[folders.length + files.length];
        System.arraycopy(folders, 0, all, 0, folders.length);
        System.arraycopy(files, 0, all, folders.length, files.length);                    
        
        
        return all;
    }

    /**
     * Overridden method.
     */
    public Object getParent(Object element) {
        IIpsElement parent;
        
        if (element instanceof IIpsPackageFragment && !(element instanceof IIpsPackageFragmentRoot)) {
            // in the model, an package-fragment is an complete package (e.g. org.faktorips.example is an
            // package-fragment. If such an fragment is asked for its parent, the project is returned! That 
            // is not what we need here - the parent of org.faktorips.example in our case here has to be
            // org.faktorips
            parent = ((IIpsPackageFragment)element).getParentIpsPackageFragment(); 
            if (parent == null) {
                // no further parents in package-hirachy, return parent in object-hirachy
                parent = ((IIpsElement)element).getParent();
            }
        }
        else {
            parent = ((IIpsElement)element).getParent(); 
        }

        if (parent != null) {
	        // ignore root, srcfiles and default-package
	        if (parent instanceof IIpsPackageFragmentRoot || parent instanceof IIpsSrcFile || (parent instanceof IIpsPackageFragment && ((IIpsPackageFragment)parent).isDefaultPacakge())) {
	            // For the department, we don't show IpsPackageFragmentRoots (children of IpsProject) and
	            // IpsObjects (children of IpsSrcFile).
	            parent = parent.getParent();
	        }
        }
        return parent;
    }

    /**
     * Overridden method.
     */
    public boolean hasChildren(Object element) {
        try {
            IIpsElement ipsElement = (IIpsElement)element;
            if (ipsElement instanceof IIpsPackageFragment) {
                return getPackageFragmentContent((IIpsPackageFragment)element).length > 0;
            }
            else if (ipsElement instanceof ITimedIpsObject) {
                return false;
            }
            else {
                return ipsElement.hasChildren();
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return false;
        }
    }

    /**
     * Overridden method.
     */
    public void dispose() {
    }

    public Object[] getElements(Object inputElement) {
        try {
            // Dont show projects which are not product-definition-projects
            IIpsProject[] projects = IpsPlugin.getDefault().getIpsModel().getIpsProjects();
            ArrayList result = new ArrayList();
            for (int i = 0; i < projects.length; i++) {
                if (projects[i].isProductDefinitionProject()) {
                    result.add(projects[i]);
                }
            }
            
            return result.toArray();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return new Object[0];
        }
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    	// nothing to do
    }
}
