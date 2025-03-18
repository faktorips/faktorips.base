/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.faktorips.devtools.core.ui.IDataChangeableReadAccess;
import org.faktorips.devtools.core.ui.IDataChangeableReadAccessWithListenerSupport;
import org.faktorips.devtools.core.ui.IpsSrcFileViewItem;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * Abstract base action for global actions.
 *
 * @author Thorsten Guenther
 * @author Stefan Widmaier
 */
public abstract class IpsAction extends Action {

    /** The source of objects to modify by this action. */
    protected ISelectionProvider selectionProvider;

    private IDataChangeableReadAccess ctrl;

    private ISelectionChangedListener adjustEnableStateListener;

    /**
     * Creates a new action. If the action is started, the given selection-provider is asked for its
     * selection and the modifications are done to the selection.
     */
    public IpsAction(ISelectionProvider selectionProvider) {
        this.selectionProvider = selectionProvider;
        if (selectionProvider != null) {
            adjustEnableStateListener = $ -> updateEnabledProperty();
            selectionProvider.addSelectionChangedListener(adjustEnableStateListener);
        }
    }

    @Override
    public void run() {
        ISelection sel = selectionProvider.getSelection();
        if (sel != null) {
            if (sel instanceof IStructuredSelection) {
                run(new StructuredSelection(mapIpsSrcFilesToIpsObjects((IStructuredSelection)sel)));
            } else {
                throw new RuntimeException(Messages.IpsAction_msgUnsupportedSelection + sel.getClass().getName());
            }
        }
    }

    /**
     * Returns a list of selected objects, map all selected IPS source files to the corresponding
     * IPS object.
     */
    private List<Object> mapIpsSrcFilesToIpsObjects(IStructuredSelection selection) {
        List<Object> selectedIpsObjects = new ArrayList<>((selection).size());
        for (Iterator<Object> iter = getSelectionIterator(selection); iter.hasNext();) {
            Object select = iter.next();
            if (select instanceof IIpsSrcFile) {
                selectedIpsObjects.add(((IIpsSrcFile)select).getIpsObject());
            } else {
                selectedIpsObjects.add(select);
            }
        }

        return selectedIpsObjects;
    }

    public abstract void run(IStructuredSelection selection);

    /**
     * This method returns all <code>IIpsObject</code>s found in the given selection. Returns an
     * empty array if the selection is empty or does not contain <code>IpsObject</code>s.
     */
    protected IIpsObject[] getIpsObjectsForSelection(IStructuredSelection selection) {
        List<IIpsObject> ipsObjects = new ArrayList<>();
        for (Iterator<Object> i = getSelectionIterator(selection); i.hasNext();) {
            ipsObjects.add(getIpsObjectForSelection(i.next()));
        }

        return ipsObjects.toArray(new IIpsObject[ipsObjects.size()]);
    }

    /**
     * Returns all <code>IIpsSrcFile</code>s found in the given selection. Returns an empty array if
     * the selection is empty or does not contain <code>IIpsSrcFiles</code>s.
     */
    protected IIpsSrcFile[] getIpsSrcFilesForSelection(IStructuredSelection selection) {
        List<IIpsSrcFile> ipsSrcFiles = new ArrayList<>();
        for (Iterator<?> i = getSelectionIterator(selection); i.hasNext();) {
            IIpsSrcFile ipsSrcFile = getIpsSrcFileForSelection(i.next());
            if (ipsSrcFile != null) {
                ipsSrcFiles.add(ipsSrcFile);
            }
        }
        return ipsSrcFiles.toArray(new IIpsSrcFile[ipsSrcFiles.size()]);
    }

    /**
     * This method returns an <code>IIpsObject</code> for the first element of the given
     * <code>StructuredSelection</code>. Returns <code>null</code> if the selection is empty or does
     * not contain the expected types.
     *
     * @see IpsAction#getIpsObjectForSelection(Object)
     */
    protected IIpsObject getIpsObjectForSelection(IStructuredSelection selection) {
        Object selected = selection.getFirstElement();
        return getIpsObjectForSelection(selected);
    }

    /**
     * This method returns an <code>IIpsObject</code> referenced by or contained in the given
     * Object.
     * <p>
     * If the given Object is of type <code>IIpsObject</code> the object itself is returned. If it
     * is an <code>IIpsObjectPart</code> the corresponding IIpsObject is returned. If the given
     * object is an array, the first element of this array is returned. This object is a
     * <code>PolicyCmptType</code>, <code>ProductCmpt</code> or <code>TestCase</code> by convention,
     * <code>null</code> is returned if this convention is violated.
     * <p>
     * If the given object is an <code>IProductCmptReference</code> the contained
     * <code>IProductCmpt</code>, if it is an <code>IProductCmptTypeAssociationReference</code> the
     * contained <code>IRelation</code>'s <code>IIpsObject</code> is returned.
     * <p>
     * If the given object is an <code>IProductCmptRelation</code> the action searches for the
     * referenced <code>IProductCmpt</code> and returns it if existent, null otherwise.
     * <p>
     * Returns <code>null</code> if the object is not an instance of the expected types.
     */
    private IIpsObject getIpsObjectForSelection(Object selected) {
        if (selected == null) {
            return null;
        }
        Object selectedObject;
        if (selected instanceof Object[]) {
            selectedObject = ((Object[])selected)[0];
        } else {
            selectedObject = selected;
        }
        if (selectedObject instanceof IIpsObject) {
            return (IIpsObject)selectedObject;
        }
        if (selectedObject instanceof IIpsSrcFile) {
            return ((IIpsSrcFile)selectedObject).getIpsObject();
        }
        if (selectedObject instanceof IIpsObjectPart) {
            return ((IIpsObjectPart)selectedObject).getIpsObject();
        }
        if (selectedObject instanceof IAdaptable adaptable) {
            IIpsSrcFile adaptedSrcFile = adaptable.getAdapter(IIpsSrcFile.class);
            if (adaptedSrcFile != null) {
                return adaptedSrcFile.getIpsObject();
            }
        }
        return null;
    }

    /**
     * Returns the <code>IIpsSrcFile</code> for the given Object. If the given object is an
     * <code>IIpsSrcFile</code> it is returned. If the given object is an <code>IIpsObject</code>,
     * the corresponding <code>IIpsSrcFile</code> is returned.
     * <p>
     * Returns <code>null</code> if the selection is empty or does not contain the expected types.
     *
     * @see IpsAction#getIpsObjectForSelection(IStructuredSelection)
     */
    private IIpsSrcFile getIpsSrcFileForSelection(Object selected) {
        if (selected instanceof IIpsSrcFile ipsSrcFile) {
            // avoid reading IpsSrcFile in getIpsObjectForSelection()
            return ipsSrcFile;
        } else if (selected instanceof IpsSrcFileViewItem viewItem) {
            return viewItem.getIpsSrcFile();
        }

        IIpsObject ipsObject = getIpsObjectForSelection(selected);
        if (ipsObject != null) {
            return ipsObject.getIpsSrcFile();
        }

        return null;
    }

    /**
     * This method returns the <code>IIpsSrcFile</code> for the given selection. The first element
     * of the <code>StructuredSelection</code> is processed. If an <code>IIpsSrcFile</code> is found
     * in the selection it is returned. If an <code>IIpsObject</code> is found in the selection, the
     * corresponding <code>IIpsSrcFile</code> is returned.
     * <p>
     * Returns <code>null</code> if the selection is empty or does not contain the expected types.
     *
     * @see IpsAction#getIpsObjectForSelection(IStructuredSelection)
     */
    protected IIpsSrcFile getIpsSrcFileForSelection(IStructuredSelection selection) {
        Object selected = selection.getFirstElement();
        return getIpsSrcFileForSelection(selected);
    }

    public void setControlWithDataChangeableSupport(IDataChangeableReadAccessWithListenerSupport ctrl) {
        this.ctrl = ctrl;
        ctrl.addDataChangeableStateChangeListener($ -> updateEnabledProperty());
    }

    /**
     * Computes and sets the new value for the enabled property.
     */
    public void updateEnabledProperty() {
        setEnabled(computeEnabledProperty());
    }

    /**
     * Returns <code>true</code> if the action should be enabled, otherwise <code>false</code>.
     * Default implementation first checks if the enabled state depends on a control with switch
     * data changeable support. If this is not the case or the data is changeable, then the property
     * is computed based on the current selection (if a selection provider is available).
     */
    protected boolean computeEnabledProperty() {
        if (ctrl != null) {
            if (!ctrl.isDataChangeable()) {
                return false;
            }
        }
        if (selectionProvider != null) {
            if (selectionProvider.getSelection() instanceof IStructuredSelection) {
                return computeEnabledProperty((IStructuredSelection)selectionProvider.getSelection());
            }
        }
        return true;
    }

    /**
     * Returns <code>true</code> if the action is enabled based on the given selection, otherwise
     * <code>false</code>. The default implementation always returns <code>true</code>.
     *
     * @param selection The user selection to check for enabled state of this action.
     */
    protected boolean computeEnabledProperty(IStructuredSelection selection) {
        return true;
    }

    public void dispose() {
        if (adjustEnableStateListener != null) {
            selectionProvider.removeSelectionChangedListener(adjustEnableStateListener);
        }
    }

    /**
     * Extracted as protected method to get along with one suppress warnings annotation.
     */
    @SuppressWarnings("unchecked")
    // Eclipse API uses unchecked iterator
    protected Iterator<Object> getSelectionIterator(IStructuredSelection selection) {
        return selection.iterator();
    }

}
