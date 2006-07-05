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
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;

/**
 * Class for calculation the content of the ModelExplorer tree. The returned
 * Lists of PackageFragments are dependant on the current layout style indicated
 * by the <code>isFlatLayout</code> flag.
 * 
 * @author Stefan Widmaier
 * 
 */
public class ModelContentProvider implements ITreeContentProvider {

	private static final Object[] EMPTY_ARRAY = new Object[0];

	private boolean isFlatLayout = false;
	
	public ModelContentProvider(){
		super();
	}
	public ModelContentProvider(boolean flatLayout){
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
				if (objects.length >= 1
						&& (objects[0] instanceof IPolicyCmptType || objects[0] instanceof ITableStructure
								|| objects[0] instanceof IProductCmpt || objects[0] instanceof ITableContents)) {
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
        		// For hierarchical layout return the PackageFragment that represents the parentfolder of this PackageFragments folder
        		// eg. org.faktorips is parent of org.faktorips.example
                parent = ((IIpsPackageFragment)element).getParentIpsPackageFragment(); 
//                System.out.println("PackageFragment: "+element+", parent: "+parent+", "+parent.getClass());
        }else{
            parent= ((IIpsElement)element).getParent();
//            System.out.println("normal getParent: "+parent+", "+parent.getClass());        	
        }

        // skip srcfiles in the object hierarchy, as in getChildren()
        if (parent != null) {
	        if (parent instanceof IIpsSrcFile) {
	            parent = parent.getParent();
//	          System.out.println("after skipping srcFile: "+parent+", "+parent.getClass());
	        }
        }
		return parent;
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	/**
	 * Returns all model-projects currently managed by the IpsModel.
	 * {@inheritDoc}
	 */
	public Object[] getElements(Object inputElement) {
        try {
			IIpsProject[] projects = IpsPlugin.getDefault().getIpsModel().getIpsProjects();
			ArrayList filteredProjects= new ArrayList();
			for(int i=0, size=projects.length; i<size; i++){
				IIpsProject proj= projects[i];
				if(proj.isModelProject()){
					filteredProjects.add(proj);
				}
			}
			return filteredProjects.toArray();
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
