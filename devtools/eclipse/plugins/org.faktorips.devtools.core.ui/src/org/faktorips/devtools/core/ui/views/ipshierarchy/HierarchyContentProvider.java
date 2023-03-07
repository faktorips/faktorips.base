/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.ipshierarchy;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.ITypeHierarchy;

/**
 * The HierarchyContentProvider is used with the Treeviewer in the class IpsHierarchyView Input
 * ITypeHierarchy Output Show the Hierarchy of an IType Object in a Treeviewer
 * 
 * @author stoll
 */
public class HierarchyContentProvider implements ITreeContentProvider {

    private ITypeHierarchy hierarchy;

    @Override
    public Object[] getChildren(Object parentElement) {
        if (hierarchy != null && parentElement instanceof IType type) {
            return hierarchy.getSubtypes(type).toArray();
        } else {
            return new Object[0];
        }
    }

    @Override
    public Object getParent(Object element) {
        if (hierarchy != null && element instanceof IType type) {
            return hierarchy.getSupertype(type);
        } else {
            return null;
        }
    }

    @Override
    public boolean hasChildren(Object element) {
        Object[] elements = getChildren(element);
        return (elements != null && elements.length != 0);
    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (hierarchy != null) {
            IType root = hierarchy.getType();
            while (hierarchy.getSupertype(root) != null) {
                root = hierarchy.getSupertype(root);
            }
            return new Object[] { root };
        }
        return new Object[] { inputElement };
    }

    @Override
    public void dispose() {
        // nothing to dispose
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput instanceof ITypeHierarchy) {
            hierarchy = (ITypeHierarchy)newInput;
        } else {
            hierarchy = null;
        }
    }

}
