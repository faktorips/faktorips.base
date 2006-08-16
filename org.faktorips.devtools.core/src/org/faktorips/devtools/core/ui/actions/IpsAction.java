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
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptReference;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.product.IProductCmptTypeRelationReference;

/**
 * Abstract base action for global actions on the ips-model.
 * 
 * @author Thorsten Guenther
 * @author Stefan Widmaier
 */
public abstract class IpsAction extends Action {

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
	protected IIpsObject[] getIpsObjectsForSelection(
			IStructuredSelection selection) {
		List ipsObjects = new ArrayList();
		for (Iterator i = selection.iterator(); i.hasNext();) {
			IIpsObject ipso = getIpsObjectForSelection(i.next());
			ipsObjects.add(ipso);
		}
		return (IIpsObject[]) ipsObjects.toArray(new IIpsObject[0]);
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
	protected IIpsSrcFile getIpsSrcFileForSelection(
			IStructuredSelection selection) {
		Object selected= selection.getFirstElement();
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
	 * Returns the apropriate Transfer for every item in the given lists in the
	 * same order as the data is returend in getDataArray.
	 */
	protected Transfer[] getTypeArray(List stringItems, List resourceItems) {
		List resultList = new ArrayList();

		if (resourceItems.size() > 0) {
			resultList.add(ResourceTransfer.getInstance());
		}

		for (int i = 0; i < stringItems.size(); i++) {
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
	 */
	protected Object[] getDataArray(List stringItems, List resourceItems) {
		List result = new ArrayList();
		if (resourceItems.size() > 0) {
			IResource[] res = new IResource[resourceItems.size()];
			result.add((IResource[]) resourceItems.toArray(res));
		}
		result.addAll(stringItems);
		return result.toArray();
	}

}
