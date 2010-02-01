/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;

public class LinkSectionContentProvider implements ITreeContentProvider {

    private IProductCmptTreeStructure structure;

    public Object[] getChildren(Object parentElement) {

        return null;
    }

    public Object getParent(Object element) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean hasChildren(Object element) {
        // only the first level of the structure should be visible - no indirect reference
        if (element instanceof IProductCmptTypeAssociationReference) {
            IProductCmptStructureReference reference = (IProductCmptStructureReference)element;
            // structure.getChild
        }
        return false;
    }

    public Object[] getElements(Object inputElement) {
        if (structure == inputElement) {
            IProductCmptReference rootCmpt = structure.getRoot();
            return structure.getChildProductCmptTypeAssociationReferences(rootCmpt, true);
        }
        return new Object[0];
    }

    public void dispose() {
        // TODO Auto-generated method stub

    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput instanceof IProductCmptTreeStructure) {
            structure = (IProductCmptTreeStructure)newInput;
        } else {
            structure = null;
        }

    }

}
