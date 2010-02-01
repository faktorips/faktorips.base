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

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;

/**
 * Provides the elements of the FaktorIps-Model for the department.
 * 
 * @author Thorsten Guenther
 */
public class DeepCopyContentProvider implements ITreeContentProvider {

    /**
     * The root-node this content provider starts to evaluate the content.
     */
    private IProductCmptTreeStructure structure;

    /**
     * Flag to tell the content provider to show (<code>true</code>) or not to show the
     * Relation-Type as Node.
     */
    private boolean fShowRelationType = true;

    private IProductCmptReference root;

    /**
     * Creates a new content provider.
     * 
     * @param showRelationType <code>true</code> to show the relation types as nodes.
     */
    public DeepCopyContentProvider(boolean showRelationType) {
        fShowRelationType = showRelationType;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getChildren(Object parentElement) {
        if (!fShowRelationType && parentElement instanceof IProductCmptReference) {
            // returns the product cmpt references without the type
            return getChildrenFor((IProductCmptReference)parentElement);
        } else if (parentElement instanceof IProductCmptReference) {
            // returns the relation type first
            return getRefChildrenFor((IProductCmptReference)parentElement);
        } else if (parentElement instanceof IProductCmptStructureReference) {
            return getChildrenFor((IProductCmptStructureReference)parentElement);
        } else {
            return new IProductCmptStructureReference[0];
        }
    }

    private Object[] getChildrenFor(IProductCmptReference parentElement) {
        return addTblUsages(parentElement, structure.getChildProductCmptReferences(parentElement));
    }

    private Object[] getRefChildrenFor(IProductCmptReference parentElement) {
        return addTblUsages(parentElement, structure.getChildProductCmptTypeAssociationReferences(parentElement));
    }

    private Object[] getChildrenFor(IProductCmptStructureReference parentElement) {
        return addTblUsages(parentElement, structure.getChildProductCmptReferences(parentElement));
    }

    private Object[] addTblUsages(IProductCmptStructureReference parentElement, Object[] productCmptReferencesOrTypes) {
        IProductCmptStructureTblUsageReference[] tblUsageReference = structure
                .getChildProductCmptStructureTblUsageReference(parentElement);
        IProductCmptStructureReference[] result = new IProductCmptStructureReference[tblUsageReference.length
                + productCmptReferencesOrTypes.length];
        System.arraycopy(productCmptReferencesOrTypes, 0, result, 0, productCmptReferencesOrTypes.length);
        System.arraycopy(tblUsageReference, 0, result, productCmptReferencesOrTypes.length, tblUsageReference.length);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Object getParent(Object element) {
        if (structure == null) {
            return null;
        }

        if (!fShowRelationType && element instanceof IProductCmptReference) {
            return structure.getParentProductCmptReference((IProductCmptReference)element);
        } else if (element instanceof IProductCmptReference) {
            return structure.getParentProductCmptTypeRelationReference((IProductCmptReference)element);
        } else if (element instanceof IProductCmptStructureReference) {
            return structure.getParentProductCmptReference((IProductCmptStructureReference)element);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasChildren(Object element) {
        return getChildren(element).length > 0;
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        structure = null;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getElements(Object inputElement) {
        if (structure == inputElement) {
            return new Object[] { root };
        } else {
            return new Object[0];
        }
    }

    /**
     * {@inheritDoc}
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput == null || !(newInput instanceof IProductCmptTreeStructure)) {
            structure = null;
            return;
        }

        structure = (IProductCmptTreeStructure)newInput;
        root = structure.getRoot();
    }

    public boolean isRelationTypeShowing() {
        return fShowRelationType;
    }

    public void setRelationTypeShowing(boolean showRelationType) {
        fShowRelationType = showRelationType;
    }
}
