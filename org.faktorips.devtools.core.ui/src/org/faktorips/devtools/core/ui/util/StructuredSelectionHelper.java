/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.util;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;

/**
 * Helper class that extracts IPS objects from a {@link IStructuredSelection}. Instances of this
 * class are immutable and do not change if selection changes.
 * 
 * @author Stefan Widmaier, FaktorZehn AG
 */
public class StructuredSelectionHelper {

    private final IStructuredSelection selection;

    /**
     * Constructs a {@link StructuredSelectionHelper} based on the given
     * {@link IStructuredSelection}. If the given selection is <code>null</code> this helper will
     * act as if having been initialized with an empty selection. No Exception is thrown in this
     * case as the platforms selection provider returns <code>null</code> in some cases.
     * 
     * @param selection the selection to extract objects from
     */
    public StructuredSelectionHelper(IStructuredSelection selection) {
        this.selection = selection == null ? new StructuredSelection() : selection;
    }

    /**
     * Returns the selections first element as an IpsObject of the given class. If the selected
     * element is a resource this method tries to acquire the corresponding {@link IIpsObject} from
     * the {@link IpsModel}. Attempts to adapt the selected element to IpsObject if it is adaptable.
     * Returns <code>null</code> if no IpsObject can be derived.
     * 
     * @param <T> Subclass of {@link IIpsObject}
     * @param clazz the expected IpsObject class
     * @return the selected object converted or adapted to {@link IIpsObject} or <code>null</code>
     *         if no IpsObject cannot be derived from the selections first element.
     */
    public <T extends IIpsObject> T getFirstElementAsIpsObject(Class<T> clazz) {
        Object selectedObject = selection.getFirstElement();
        if (selectedObject instanceof IResource) {
            IResource resource = (IResource)selectedObject;
            IIpsElement ipsElement = IpsPlugin.getDefault().getIpsModel().getIpsElement(resource);
            return adaptToIpsObjectType(ipsElement, clazz);
        } else if (selectedObject instanceof IAdaptable) {
            return adaptToIpsObjectType((IAdaptable)selectedObject, clazz);
        }
        return null;
    }

    private <T extends IIpsObject> T adaptToIpsObjectType(IAdaptable adaptable, Class<T> clazz) {
        if (adaptable == null) {
            return null;
        }
        IIpsObject ipsObject = (IIpsObject)adaptable.getAdapter(IIpsObject.class);
        if (ipsObject != null && clazz.isAssignableFrom(ipsObject.getClass())) {
            return clazz.cast(ipsObject);
        }
        return null;
    }
}
