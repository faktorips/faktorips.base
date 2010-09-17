/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.ipshierarchy;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.ITypeHierarchy;

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
        if (hierarchy != null && parentElement instanceof IType) {
            IType type = (IType)parentElement;
            return hierarchy.getSubtypes(type);
        } else {
            return new Object[0];
        }
    }

    @Override
    public Object getParent(Object element) {
        if (hierarchy != null && element instanceof IType) {
            IType type = (IType)element;
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

    public ITypeHierarchy getTypeHierarchy() {
        return this.hierarchy;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput instanceof ITypeHierarchy) {
            this.hierarchy = (ITypeHierarchy)newInput;
        } else {
            hierarchy = null;
        }
    }
}
