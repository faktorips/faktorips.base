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

package org.faktorips.devtools.core.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.ResourceTransfer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptReference;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.product.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.product.IProductCmptTypeRelationReference;
import org.faktorips.util.StringUtil;

/**
 * Abstract base action for global actions.
 * 
 * @author Thorsten Guenther
 * @author Stefan Widmaier
 */
public abstract class IpsAction extends Action {
    private static final String ARCHIVE_LINK = "ARCHIVE_LINK"; //$NON-NLS-1$
    
	/**
	 * The source of objects to modify by this action.
	 */
	protected ISelectionProvider selectionProvider;

	private ISelection selection = null;

	/**
	 * Creates a new IpsAction. This action uses the
	 * <code>SelectionService</code> of the given WorkbenchWindow to retrieve
	 * the current selection. Actions created with this constructor are
	 * independant of the viewpart they are ariginally created in.
	 * <p>
	 * This constructor must not be used for actions in contextmenus, as there
	 * are problems with the selectionservice when the popupmenu opens.
	 */
	public IpsAction(IWorkbenchWindow workbenchWindow) {
		this.selectionProvider = null;
	}

	/**
	 * Creates a new action. If the action is started, the given
	 * selection-provider is asked for its selection and the modifications are
	 * done to the selection.
	 * 
	 * @param selectionProvider
	 */
	public IpsAction(ISelectionProvider selectionProvider) {
		this.selectionProvider = selectionProvider;
	}

	public void run() {
		ISelection sel = selectionProvider.getSelection();
		if (sel != null) {
			if (sel instanceof IStructuredSelection) {
				run((IStructuredSelection) sel);
			} else {
				throw new RuntimeException(
						Messages.IpsAction_msgUnsupportedSelection
								+ selection.getClass().getName());
			}
		}
	}

	abstract public void run(IStructuredSelection selection);

    /**
     * This method returns all <code>IIpsObject</code>s found in the given
     * selection. Returns an empty array if the selection is empty or
     * does not contain <code>IpsObject</code>s.
     */
    protected IIpsObject[] getIpsObjectsForSelection(IStructuredSelection selection) {
        List ipsObjects = new ArrayList();
        for (Iterator i = selection.iterator(); i.hasNext();) {
            ipsObjects.add(getIpsObjectForSelection(i.next()));
        }
        return (IIpsObject[]) ipsObjects.toArray(new IIpsObject[ipsObjects.size()]);
    }
    /**
     * Returns all <code>IIpsSrcFile</code>s found in the given
     * selection. Returns an empty array if the selection is empty or
     * does not contain <code>IIpsSrcFiles</code>s.
     */
    protected IIpsSrcFile[] getIpsSrcFilesForSelection(IStructuredSelection selection) {
        List ipsSrcFiles = new ArrayList();
        for (Iterator i = selection.iterator(); i.hasNext();) {
            ipsSrcFiles.add(getIpsSrcFileForSelection(i.next()));
        }
        return (IIpsSrcFile[]) ipsSrcFiles.toArray(new IIpsSrcFile[ipsSrcFiles.size()]);
    }

	/**
	 * This method returns an <code>IIpsObject</code> for the first element of
	 * the given <code>StructuredSelection</code>. Returns <code>null</code> 
	 * if the selection is empty or does not contain the expected types.
	 * @see IpsAction#getIpsObjectForSelection(Object)
	 */
	protected IIpsObject getIpsObjectForSelection(IStructuredSelection selection) {
		Object selected = selection.getFirstElement();
		return getIpsObjectForSelection(selected);
	}

	/**
	 * This method returns an <code>IIpsObject</code> referenced by or
	 * contained in the given Object.
	 * <p>
	 * If the given Object is of type <code>IIpsObject</code> the object
	 * itself is returned. If it is an <code>IIpsObjectPart</code> the
	 * corresponding IIpsObject is returned. If the given object is an array,
	 * the first element of this array is returned. This object is a
	 * <code>PolicyCmptType</code> or <code>ProductCmpt</code> by
	 * convention, <code>null</code> is returned if this convention is
	 * violated.
	 * <p>
	 * If the given object is an <code>IProductCmptReference</code> the
	 * contained <code>IProductCmpt</code>, if it is an
	 * <code>IProductCmptTypeRelationReference</code> the contained
	 * <code>IRelation</code>'s <code>IIpsObject</code> is returned.
	 * <p>
	 * If the given object is an <code>IProductCmptRelation</code> the action
	 * searches for the referenced <code>IProductCmpt</code> and returns it
	 * if existent, null otherwise.
	 * <p>
	 * Returns <code>null</code> if the object is not an instance of the
	 * expected types.
	 */
	private IIpsObject getIpsObjectForSelection(Object selected) {
		if (selected == null) {
			// empty selection
			return null;
		}

		if (selected instanceof IIpsObject) {
			return (IIpsObject) selected;
		}
		if (selected instanceof IIpsSrcFile) {
			try {
				return ((IIpsSrcFile) selected).getIpsObject();
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
		}
		if (selected instanceof IIpsObjectPart) {
			return ((IIpsObjectPart) selected).getIpsObject();
		}
		// for use with StructureExplorer: open ProductComponents contained in
		// StructureNodes
		if (selected instanceof IProductCmptReference) {
			return ((IProductCmptReference) selected).getProductCmpt();
		}
		if (selected instanceof IProductCmptTypeRelationReference) {
			return ((IProductCmptTypeRelationReference) selected).getRelation()
					.getIpsObject();
		}
		if (selected instanceof IProductCmptRelation) {
			try {
				IProductCmptRelation rel = (IProductCmptRelation) selected;
				return rel.getIpsProject().findIpsObject(
						IpsObjectType.PRODUCT_CMPT, rel.getTarget());
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
		}
        // for use with StructureExplorer: open TableContent contained in
        // StructureNodes
        if (selected instanceof IProductCmptStructureTblUsageReference) {
            try {
                return ((IProductCmptStructureTblUsageReference) selected).getTableContentUsage().findTableContents();
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }        
		// for use with reference search, where elements are stored in arrays
		if (selected instanceof Object[]) {
			Object[] array = (Object[]) selected;
			if (array.length > 1 && array[0] instanceof IProductCmpt) {
				return (IIpsObject) array[0];
			} else if (array.length >= 1 && array[0] instanceof IPolicyCmptType) {
				return (IIpsObject) array[0];
			}
		}
		return null;
	}

    /**
     * Returns the <code>IIpsSrcFile</code> for the given Object. 
     * If the given object is an <code>IIpsSrcFile</code> it is
     * returned. If the given object is an <code>IIpsObject</code>, the
     * corresponding <code>IIpsSrcFile</code> is returned.
     * <p>
     * Returns <code>null</code> if the selection is empty or does not contain
     * the expected types.
     * @see IpsAction#getIpsObjectForSelection(IStructuredSelection)
     */
    private IIpsSrcFile getIpsSrcFileForSelection(Object selected) {
        if(selected instanceof IIpsSrcFile){
            // avoid reading IpsSrcFile in getIpsObjectForSelection()
            return (IIpsSrcFile) selected;
        }
        IIpsObject ipsObject = getIpsObjectForSelection(selected);
        if (ipsObject != null) {
            return ipsObject.getIpsSrcFile();
        }
        return null;
    }
    
	/**
	 * This method returns the <code>IIpsSrcFile</code> for the given
	 * selection. The first element of the <code>StructuredSelection</code> is
	 * processed. If an <code>IIpsSrcFile</code> is found in the selection it is
	 * returned. If an <code>IIpsObject</code> is found in the selection, the
	 * corresponding <code>IIpsSrcFile</code> is returned.
	 * <p>
	 * Returns <code>null</code> if the selection is empty or does not contain
	 * the expected types.
	 * @see IpsAction#getIpsObjectForSelection(IStructuredSelection)
	 */
	protected IIpsSrcFile getIpsSrcFileForSelection(IStructuredSelection selection) {
		Object selected= selection.getFirstElement();
        return getIpsSrcFileForSelection(selected);
	}

	/**
	 * Returns the apropriate Transfer for every item in the given lists in the
	 * same order as the data is returend in getDataArray.
	 */
	protected Transfer[] getTypeArray(List stringItems, List resourceItems, List copiedResourceLinks) {
		List resultList = new ArrayList();
		if (resourceItems.size() > 0) {
			resultList.add(ResourceTransfer.getInstance());
		}
        for (int i = 0; i < stringItems.size(); i++) {
			resultList.add(TextTransfer.getInstance());
		}
        if (copiedResourceLinks != null && copiedResourceLinks.size() > 0 ){
            // the links will be merged to one text inside the clipboard
            resultList.add(TextTransfer.getInstance());
        }
		Transfer[] result = new Transfer[resultList.size()];
		return (Transfer[]) resultList.toArray(result);
	}

	/**
	 * Builds the data-array for clipboard operations (copy, drag,...).
	 * 
	 * @param stringItems
	 *            The list of strings to put to the clipboard
	 * @param resourceItems
	 *            The list of resources to put to the clipboard
	 * @param copiedResourceLinks 
     *            The list of resource links to put in the clipboard
	 */
	protected Object[] getDataArray(List stringItems, List resourceItems, List copiedResourceLinks) {
		List result = new ArrayList();
        // add copied resources
        if (resourceItems.size() > 0) {
            IResource[] res = new IResource[resourceItems.size()];
            result.add((IResource[])resourceItems.toArray(res));
        }
        // add copied text
        result.addAll(stringItems);
        // add copied resource links (e.g. links to an object inside an archiv)
        //   all links will be merged to one text inside the clipboard
        if (copiedResourceLinks != null) {
            String strReferences = ""; //$NON-NLS-1$
            for (Iterator iter = copiedResourceLinks.iterator(); iter.hasNext();) {
                String element = (String)iter.next();
                strReferences += strReferences.length() > 0 ? "," + element : element; //$NON-NLS-1$
            }
            if (StringUtils.isNotEmpty(strReferences)) {
                result.add(ARCHIVE_LINK + strReferences);
            }
        }
		return result.toArray();
	}

    public String getResourceLinkInArchive(IIpsObject ipsObject) {
        IIpsPackageFragmentRoot root = ipsObject.getIpsPackageFragment().getRoot();
        String srcFileName = ipsObject.getIpsSrcFile().getName();
        String content = root.getIpsProject().getName() + "#" + root.getName() + "#" + ipsObject.getQualifiedName() //$NON-NLS-1$ //$NON-NLS-2$
                + "#" + StringUtil.getFileExtension(srcFileName); //$NON-NLS-1$
        return content;
    }
    
    public String getResourceLinkInArchive(IIpsPackageFragment fragment) {
        IIpsPackageFragmentRoot root = fragment.getRoot();
        String content = root.getIpsProject().getName() + "#" + root.getName() + "#" + fragment.getName(); //$NON-NLS-1$ //$NON-NLS-2$
        return content;
    }
    
    private final static IIpsObject[] EMPTY_IPS_OBJECT_ARRAY = new IIpsObject[0];;
    
    /**
     * Returns all objects which are represented by links inside the clipboard. If there are no resource links inside the clipboard an
     * empty array will ne returned. If the linked object wasn't found then the object will be ignored (not returned).
     */
    public Object[] getObjectsFromResourceLinks(String resourceLinks){
        if (resourceLinks == null || ! resourceLinks.startsWith(ARCHIVE_LINK)){
            // no resource links
            return EMPTY_IPS_OBJECT_ARRAY;
        }
        resourceLinks = resourceLinks.substring(ARCHIVE_LINK.length(), resourceLinks.length()); 

        StringTokenizer tokenizer = new StringTokenizer(resourceLinks, ","); //$NON-NLS-1$
        int count = tokenizer.countTokens();
        List result = new ArrayList(1);
        List links = new ArrayList(count);

        for (int i = 0; tokenizer.hasMoreTokens(); i++) {
            links.add(tokenizer.nextToken());
        }
        
        for (Iterator iter = links.iterator(); iter.hasNext();) {
            String resourceLink = (String)iter.next();
            
            String[] copiedResource = StringUtils.split(resourceLink, "#"); //$NON-NLS-1$
            // 1. find the project
            IIpsProject project = IpsPlugin.getDefault().getIpsModel().getIpsProject(copiedResource[0]);
            try {
                // 2. find the root
                IIpsPackageFragmentRoot[] roots = project.getIpsPackageFragmentRoots();
                IIpsPackageFragmentRoot archive = null;
                for (int i = 0; i < roots.length; i++) {
                    if (roots[i].getName().equals(copiedResource[1])) {
                        archive = roots[i];
                        break;
                    }
                }
                if (archive == null) {
                    continue;
                }
                // 3. find the object or package
                if (copiedResource.length >= 4) {
                    // the link represents an object (object [3] contains the type of the object)
                    //   try to find the object
                    IIpsObject ipsObject = archive.findIpsObject(IpsObjectType.getTypeForExtension(copiedResource[3]),
                            copiedResource[2]);
                    if (ipsObject != null) {
                        result.add(ipsObject);
                    }
                } else {
                    // the link represents a package fragment
                    //   try to obtain the package fragment
                    IIpsPackageFragment packageFrgmt = archive.getIpsPackageFragment(copiedResource[2]);
                    if (packageFrgmt != null) {
                        result.add(packageFrgmt);
                    }
                }
            } catch (Exception e) {
                IpsPlugin.log(e);
            }
        }
        return result.toArray();
    }

}
