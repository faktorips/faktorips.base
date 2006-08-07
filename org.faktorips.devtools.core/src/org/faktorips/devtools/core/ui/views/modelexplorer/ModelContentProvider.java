package org.faktorips.devtools.core.ui.views.modelexplorer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.ui.views.productdefinitionexplorer.ProductExplorerFilter;

/**
 * Class for calculation the content of the ModelExplorer tree. The returned
 * Lists of PackageFragments are dependant on the current layout style indicated
 * by the <code>isFlatLayout</code> flag.
 * @author Stefan Widmaier
 */
public class ModelContentProvider implements ITreeContentProvider {

	private static final Object[] EMPTY_ARRAY = new Object[0];

	private boolean isFlatLayout = false;
	
	private ModelExplorerConfiguration configuration;
	
	/**
	 * Constructs a ModelContentProvider with the given Configuration and the given 
	 * layout style.
	 */
	public ModelContentProvider(ModelExplorerConfiguration config, boolean flatLayout){
		configuration= config;
		isFlatLayout= flatLayout;
	}

	/**
	 * {@inheritDoc}
	 */

	public Object[] getChildren(Object parentElement) {
		return filter(getChildrenUnfiltered(parentElement));
	}
	
	private Object[] getChildrenUnfiltered(Object parentElement) {
		if(parentElement instanceof IIpsElement){
			if (parentElement instanceof IAttribute || parentElement instanceof IProductCmpt) {
				return EMPTY_ARRAY;
			}
			try {
				if (parentElement instanceof IIpsProject) {
					IIpsProject proj= (IIpsProject) parentElement;
					return concatenate(proj.getIpsPackageFragmentRoots(), proj.getNonIpsResources());
				} else if (parentElement instanceof IIpsPackageFragmentRoot) {
					IIpsPackageFragmentRoot root = (IIpsPackageFragmentRoot) parentElement;
					if (isFlatLayout) {
						return concatenate(root.getIpsPackageFragments(), root.getNonIpsResources());
					}else{
						return getPackageFragmentContent(root.getIpsDefaultPackageFragment());
					}
				} else if (parentElement instanceof IIpsPackageFragment) {
					return getPackageFragmentContent((IIpsPackageFragment) parentElement);
				} else {
					return ((IIpsElement) parentElement).getChildren();
				}
			} catch (CoreException e) {
				IpsPlugin.log(e);
				return EMPTY_ARRAY;
			}
		}else if(parentElement instanceof IResource){
			IWorkbenchAdapter adapter= null;
			if (parentElement instanceof IAdaptable){
				adapter= (IWorkbenchAdapter) ((IAdaptable) parentElement).getAdapter(IWorkbenchAdapter.class);
			}
	        if (adapter != null) {
	            return adapter.getChildren(parentElement);
	        }
	        return new Object[0];
		}else{
			return EMPTY_ARRAY;
		}
	}
    
	/**
	 * Returns IpsObjects contained in sourcefiles in the given PackageFragment.
	 * For hierarchical layout subPackageFragments are also returned.
	 * 
	 * @throws CoreException
	 */
	private Object[] getPackageFragmentContent(IIpsPackageFragment fragment)
			throws CoreException {
		Object[] folders = null;
		if (!isFlatLayout) {
			folders = fragment.getIpsChildPackageFragments();
		} else {
			folders = EMPTY_ARRAY;
		}
		IIpsElement[] files = fragment.getChildren();

		List pcts = new ArrayList();
		for (int i = 0, size = files.length; i < size; i++) {
			if (files[i] instanceof IIpsSrcFile) {
            	IFile file = ((IIpsSrcFile)files[i]).getCorrespondingFile();
            	if (!file.isSynchronized(IResource.DEPTH_ZERO)) {
            		file.getParent().refreshLocal(IResource.DEPTH_ONE, null);
            	}
            	pcts.add(((IIpsSrcFile)files[i]).getIpsObject());
			}
		}
		/* Add nonIPS files after reading IpsElements.
		 * This array of objects cannot contain any folders, as all folders
		 * contained in a IpsPackageFragmentRoot are interpreted as IpsPackageFragments.
		 */
		Object[] filesNonIps = fragment.getNonIpsResources();
		return concatenate(concatenate(folders, pcts.toArray()), filesNonIps);
	}
	
	/**
	 * Returns a new object array containig all <code>IIpsElement</code>s
	 * and <code>IResource</code>s allowed by the configuration as well as
	 * all visible IResources in the given object array.
	 */
	private Object[] filter(Object[] elements){
		List filtered= new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			if(elements[i] instanceof IIpsElement){
				if(configuration.isAllowedIpsElement(elements[i])){
					filtered.add(elements[i]);
				}
			}else if(elements[i] instanceof IResource){
//				 filter out hidden files and folders
				if(elements[i] instanceof IFile | elements[i] instanceof IFolder){
					if(((IResource)elements[i]).getName().indexOf(".")==0){
						continue;
					}
				}
				if(elements[i] instanceof IFile){
					if(((IResource)elements[i]).getName().endsWith(".class")){
						continue;
					}
				}
				if(configuration.isAllowedResource(elements[i])){
					filtered.add(elements[i]);
				}
			}
		}
		return filtered.toArray();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Object getParent(Object element) {
		if(element instanceof IIpsElement){
			IIpsElement parent;
	        if(!isFlatLayout && element instanceof IIpsPackageFragment){
	    		/* For hierarchical layout return the PackageFragment that represents the 
	    		 * parentfolder of this PackageFragments folder, eg. org.faktorips is parent of 
	    		 * org.faktorips.example.  
	    		 */
	        	parent = ((IIpsPackageFragment)element).getParentIpsPackageFragment(); 
	        }else{
	            parent= ((IIpsElement)element).getParent();       	
	        }
	
	        // skip srcfiles in the object hierarchy, as in getChildren()
	        if (parent != null) {
		        if (parent instanceof IIpsSrcFile) {
		            parent = parent.getParent();
		        }
	        }
			return parent;
		}else{
			IWorkbenchAdapter adapter= null;
			if (element instanceof IAdaptable){
				adapter= (IWorkbenchAdapter) ((IAdaptable) element).getAdapter(IWorkbenchAdapter.class);
			}
	        if (adapter != null) {
	            return adapter.getParent(element);
	        }
	        return null;
		}
	}
	
	/**
	 * For IIpsElements this method returns true if the element contains 
	 * at least one child, that is of a type allowed by the ModelExplorerConfiguration.
	 */
	public boolean hasChildren(Object element) {
		return getChildren(element).length >= 1;
	}

	/**
	 * Returns all model-projects currently managed by the IpsModel. An empty
	 * array is returned if no such projects are found, or if the given inputElement
	 * is not an <code>IIpsModel</code>. <p>
	 * The IpsProjects are filtered by the <code>ModelExplorerFilter</code>.
	 * @see ProductExplorerFilter
	 * {@inheritDoc}
	 */
	public Object[] getElements(Object inputElement) {
		if(inputElement instanceof IIpsModel){
	        try {
	        	IIpsModel model= (IIpsModel)inputElement;
	        	return concatenate(model.getIpsProjects(), model.getNonIpsResources());
			} catch (CoreException e) {
				IpsPlugin.log(e);
				return EMPTY_ARRAY;
			}
		}else{
			return EMPTY_ARRAY;
		}
	}

	public void dispose() {

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	/**
	 * Sets the flag for flat respectivly hierarchical PackageFragments
	 * 
	 * @param b
	 */
	/* package */ void setIsFlatLayout(boolean b) {
		isFlatLayout = b;
	}
	
	/**
	 * Concatenates arr1 and arr2 to a new <code>Object[]</code> with length 
	 * <code>arr1.length+arr2.length</code>.
	 */
	private static Object[] concatenate(Object[] arr1, Object[] arr2){
		int length1= arr1.length;
		int length2= arr2.length;
		Object[] result= new Object[length1+length2];
		System.arraycopy(arr1, 0, result, 0, length1);
		System.arraycopy(arr2, 0, result, length1, length2);
		return result;
	}

}
