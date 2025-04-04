/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;

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

    /*
     * Flag to tell the content provider to show (<code>true</code>) or not to show the
     * Relation-Type as Node.
     */
    private boolean showRelationType = true;

    /*
     * Flag to show or hide association targets
     */
    private boolean showAssociationTargets = true;

    private IProductCmptReference root;

    /**
     * Creates a new content provider.
     *
     * @param showRelationType <code>true</code> to show the relation types as nodes.
     */
    public DeepCopyContentProvider(boolean showRelationType, boolean showAssociationTargets) {
        this.showRelationType = showRelationType;
        this.showAssociationTargets = showAssociationTargets;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        return switch (parentElement) {
            // returns the product cmpt references without the type
            case IProductCmptReference productCmptReference when !showRelationType -> getChildrenFor(
                    productCmptReference);
            // returns the relation type first
            case IProductCmptReference productCmptReference -> getRefChildrenFor((IProductCmptReference)parentElement);
            case IProductCmptStructureReference productCmptStructureReference -> getChildrenFor(
                    productCmptStructureReference);
            default -> new IProductCmptStructureReference[0];
        };
    }

    private Object[] getChildrenFor(IProductCmptReference parentElement) {
        return addTblUsages(parentElement, structure.getChildProductCmptReferences(parentElement));
    }

    private Object[] getRefChildrenFor(IProductCmptReference parentElement) {
        return addTblUsages(parentElement,
                structure.getChildProductCmptTypeAssociationReferences(parentElement, false));
    }

    private Object[] getChildrenFor(IProductCmptStructureReference parentElement) {
        return addTblUsages(parentElement, structure.getChildProductCmptReferences(parentElement));
    }

    private Object[] addTblUsages(IProductCmptStructureReference parentElement, Object[] productCmptReferencesOrTypes) {
        Object[] filteredElemens = filterAssociationTargets(productCmptReferencesOrTypes, showAssociationTargets);
        IProductCmptStructureTblUsageReference[] tblUsageReference = structure
                .getChildProductCmptStructureTblUsageReference(parentElement);
        IProductCmptStructureReference[] result = new IProductCmptStructureReference[tblUsageReference.length
                + filteredElemens.length];
        System.arraycopy(filteredElemens, 0, result, 0, filteredElemens.length);
        System.arraycopy(tblUsageReference, 0, result, filteredElemens.length, tblUsageReference.length);
        return result;
    }

    private Object[] filterAssociationTargets(Object[] productCmptReferencesOrTypes, boolean showAssociationTargets) {
        if (showAssociationTargets) {
            // return un-filtered list
            return productCmptReferencesOrTypes;
        }
        // filter association targets from the list
        List<Object> result = new ArrayList<>();
        for (Object productCmptReferencesOrType : productCmptReferencesOrTypes) {
            if (productCmptReferencesOrType instanceof IProductCmptTypeAssociationReference productCmptTypeRelationReference) {
                if (productCmptTypeRelationReference.getAssociation().isAssoziation()) {
                    continue;
                }
            }
            result.add(productCmptReferencesOrType);
        }
        return result.toArray(new Object[result.size()]);
    }

    @Override
    public Object getParent(Object element) {
        if (structure == null) {
            return null;
        }

        if (element instanceof IProductCmptReference productCmptReference) {
            if (!showRelationType) {
                return structure.getParentProductCmptReference(productCmptReference);
            } else {
                return structure.getParentProductCmptTypeRelationReference(productCmptReference);
            }
        } else if (element instanceof IProductCmptStructureReference productCmptStructureReference) {
            return structure.getParentProductCmptReference(productCmptStructureReference);
        }

        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        return getChildren(element).length > 0;
    }

    @Override
    public void dispose() {
        structure = null;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (structure == inputElement) {
            return new Object[] { root };
        } else {
            return new Object[0];
        }
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput == null || !(newInput instanceof IProductCmptTreeStructure)) {
            structure = null;
            return;
        }

        structure = (IProductCmptTreeStructure)newInput;
        root = structure.getRoot();
    }

    public boolean isRelationTypeShowing() {
        return showRelationType;
    }

    /**
     * Returns the element in the structure which is the root
     */
    protected IProductCmptReference getRoot() {
        return root;
    }

    /**
     * Returns <code>true</code> id association targets are visible or not
     */
    public boolean isShowAssociationTargets() {
        return showAssociationTargets;
    }
}
