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
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.type.IType;

/**
 * 
 * Input ITypeHierarchy Show the Hierarchy in a Treeviewer Output IType
 * 
 * @author stoll
 */
public class HierarchyContentProvider implements ITreeContentProvider {

    private ITypeHierarchy hierarchy;

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof IType) {
            IType type = (IType)parentElement;
            return hierarchy.getSubtypes(type);
        } else {
            return new Object[0];
        }
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof IType) {
            IType type = (IType)element;
            return hierarchy.getSupertype(type);
        } else {
            return null;
        }
    }

    @Override
    public boolean hasChildren(Object element) {
        return (getChildren(element) != null && getChildren(element).length != 0);
    }

    @Override
    public Object[] getElements(Object inputElement) {
        IType root = hierarchy.getType();
        while (hierarchy.getSupertype(root) != null) {
            root = hierarchy.getSupertype(root);
        }
        return new Object[] { root };
    }

    @Override
    public void dispose() {
        // nothing to dispose
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput instanceof ITypeHierarchy) {
            hierarchy = (ITypeHierarchy)newInput;
        }

    }

    public ITypeHierarchy getTypeHierarchy() {
        return this.hierarchy;
    }
}
