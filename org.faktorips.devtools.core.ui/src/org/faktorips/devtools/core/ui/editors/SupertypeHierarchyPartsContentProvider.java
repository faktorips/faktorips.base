/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

/**
 * <tt>SupertypeHierarchyPartsContentProvider</tt>s provide a set of <tt>IIpsObjectPart</tt>s from
 * the supertype hierarchy of an <tt>IIpsObject</tt>.
 * 
 * @author Alexander Weickmann
 */
public abstract class SupertypeHierarchyPartsContentProvider implements ITreeContentProvider {

    /** The set of provided <tt>IIpsObjectPart</tt>s. */
    private List<? extends IIpsObjectPart> providedObjectParts;

    /**
     * The supertypes building the supertype hierarchy to which the provided <tt>IIpsObjectPart</tt>
     * s belong to.
     */
    private List<? extends IIpsObject> supertypes;

    /**
     * @param ipsObject The <tt>IIpsObject</tt> the <tt>IIpsObjectPart</tt>s available for selection
     *            belong to.
     */
    public SupertypeHierarchyPartsContentProvider(IIpsObject ipsObject) {
        supertypes = getSupertypes(ipsObject);
        providedObjectParts = getAvailableParts(ipsObject);
        reverseSupertypes();
    }

    protected abstract List<? extends IIpsObject> getSupertypes(IIpsObject ipsObject);

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof IIpsObject) {
            IIpsObject ipsObject = (IIpsObject)parentElement;
            List<IIpsObjectPart> parts = new ArrayList<IIpsObjectPart>();
            for (IIpsObjectPart providedObjectPart : providedObjectParts) {
                if (providedObjectPart.getIpsObject().equals(ipsObject)) {
                    parts.add(providedObjectPart);
                }
            }
            return parts.toArray();
        }
        return new Object[0];
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof IIpsObject) {
            return null;
        }
        if (element instanceof IIpsObjectPart) {
            return ((IIpsObjectPart)element).getParent();
        }
        throw new RuntimeException("Unknown element " + element); //$NON-NLS-1$
    }

    @Override
    public boolean hasChildren(Object element) {
        return getChildren(element).length > 0;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return isNoAvailableParts() ? new Object[0] : supertypes.toArray();
    }

    private void reverseSupertypes() {
        Collections.reverse(supertypes);
    }

    private boolean isNoAvailableParts() {
        for (IIpsObject ipsObject : supertypes) {
            if (hasChildren(ipsObject)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void dispose() {
        // Nothing to do.
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // Nothing to do.
    }

    protected abstract List<? extends IIpsObjectPart> getAvailableParts(IIpsObject ipsObject);

}
