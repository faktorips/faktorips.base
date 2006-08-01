package org.faktorips.devtools.core.ui.views.modelexplorer;

import java.util.ArrayList;
import java.util.List;

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
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.product.IProductCmpt;

/**
 * Class for calculation the content of the ModelExplorer tree. The returned
 * Lists of PackageFragments are dependant on the current layout style indicated
 * by the <code>isFlatLayout</code> flag.
 * @author Stefan Widmaier
 * 
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
		if (!(parentElement instanceof IIpsElement)
				|| parentElement instanceof IAttribute
				|| parentElement instanceof IProductCmpt) {
			return EMPTY_ARRAY;
		}
		try {
			if (parentElement instanceof IIpsProject) {
				return ((IIpsProject) parentElement)
						.getIpsPackageFragmentRoots();
			} else if (parentElement instanceof IIpsPackageFragmentRoot) {
				IIpsPackageFragmentRoot root = (IIpsPackageFragmentRoot) parentElement;
				if (isFlatLayout) {
					return root.getIpsPackageFragments();
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
				IIpsElement[] objects = files[i].getChildren();
				if (objects.length > 1) {
					IpsPlugin.log(new IpsStatus(Messages.ModelContentProvider_tooManyIpsObjectsFoundInSrcFile));
				}
				if (objects.length >= 1){
					pcts.add(objects[0]);
				}
			}
		}

		Object all[] = new Object[folders.length + pcts.size()];
		System.arraycopy(folders, 0, all, 0, folders.length);
		System.arraycopy(pcts.toArray(EMPTY_ARRAY), 0, all,
				folders.length, pcts.size());

		return all;
	}

	public Object getParent(Object element) {
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
	}
	
	/**
	 * For IIpsElements this method returns true if the element contains 
	 * at least one child, that is of a type allowed by the ModelExplorerConfiguration.
	 */
	public boolean hasChildren(Object element) {
		if (element instanceof IIpsElement){
			if (element instanceof IAttribute
					|| element instanceof IProductCmpt) {
				return false;
			}
			Object[] children= getChildren(element); // TODO handling for arbitrary resources
			/* If an element contains only objects that are not 
			 * allowed by the configuration, it should be marked as empty.
			 * e.g. a PackageFragment contains only ProductCmpts or a
			 * PolicyCmptType contains only Attributes.
			 */
			boolean hasChildren= false;
			for(int i=0, size=children.length; i<size; i++){
				// element contains at least one child 
				if(configuration.isAllowedIpsElementType(children[i].getClass())){
					// at least one object is allowed: element has children
					hasChildren= true;
					break;
				}
			}
			return hasChildren;
		}else{
			// resource types
			return false;
		}
	}

	/**
	 * Returns all model-projects currently managed by the IpsModel.
	 * The IpsProjects are filtered by the <code>ModelExplorerFilter</code>.
	 * @see ModelExplorerFilter
	 * {@inheritDoc}
	 */
	public Object[] getElements(Object inputElement) {
        try {
        	return IpsPlugin.getDefault().getIpsModel().getIpsProjects();
		} catch (CoreException e) {
			IpsPlugin.log(e);
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

}
