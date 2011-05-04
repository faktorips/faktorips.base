/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;

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

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] getChildren(Object parentElement) {
        if (!showRelationType && parentElement instanceof IProductCmptReference) {
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
        return addTblUsages(parentElement, structure.getChildProductCmptTypeAssociationReferences(parentElement, false));
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
        List<Object> result = new ArrayList<Object>();
        for (Object productCmptReferencesOrType : productCmptReferencesOrTypes) {
            if (productCmptReferencesOrType instanceof IProductCmptTypeAssociationReference) {
                IProductCmptTypeAssociationReference productCmptTypeRelationReference = (IProductCmptTypeAssociationReference)productCmptReferencesOrType;
                if (productCmptTypeRelationReference.getAssociation().isAssoziation()) {
                    continue;
                }
            }
            result.add(productCmptReferencesOrType);
        }
        return result.toArray(new Object[result.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getParent(Object element) {
        if (structure == null) {
            return null;
        }

        if (element instanceof IProductCmptReference) {
            if (!showRelationType) {
                return structure.getParentProductCmptReference((IProductCmptReference)element);
            } else {
                return structure.getParentProductCmptTypeRelationReference((IProductCmptReference)element);
            }
        } else if (element instanceof IProductCmptStructureReference) {
            return structure.getParentProductCmptReference((IProductCmptStructureReference)element);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasChildren(Object element) {
        return getChildren(element).length > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        structure = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
