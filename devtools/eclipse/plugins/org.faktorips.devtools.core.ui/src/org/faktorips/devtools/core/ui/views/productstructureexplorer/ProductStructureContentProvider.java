/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;

/**
 * Provides the elements of product structure
 *
 * @author Thorsten Guenther
 */
public class ProductStructureContentProvider implements ITreeContentProvider {

    /**
     * The root-node this content provider starts to evaluate the content.
     */
    private IProductCmptTreeStructure structure;

    /**
     * Flag to tell the content provider to show (<code>true</code>) or not to show the
     * Association-Type as Node.
     */
    private boolean showAssociationNodes = true;

    /**
     * Flag indicating whether or not tableContents are to be returned by
     * {@link #getChildren(Object)}.
     */
    private boolean showTableContents = true;

    /**
     * Flag indicating whether validation rules are to be returned by {@link #getChildren(Object)}.
     */
    private boolean showValidationRules = true;

    /**
     * Flag indicating whether or not associated product components are to be returned by
     * {@link #getChildren(Object)} in addition to all product components in a composite relation
     * with the respective product component.
     */
    private boolean showAssociatedCmpts;

    /**
     * Creates a new content provider.
     */
    public ProductStructureContentProvider(boolean showAssociationNodes) {
        this.showAssociationNodes = showAssociationNodes;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        List<IProductCmptStructureReference> children = new ArrayList<>();

        // TODO Entwicklungsstand SMART-MODE
        // // add product cmpt associations and product cmpts
        // if (parentElement instanceof IProductCmptReference) {
        // IProductCmptTypeAssociationReference[] ralationReferences = structure
        // .getChildProductCmptTypeRelationReferences((IProductCmptReference)parentElement, false);
        // if (ralationReferences.length > 1) {
        // children.addAll(Arrays.asList(ralationReferences));
        // } else {
        // children.addAll(Arrays.asList(structure
        // .getChildProductCmptReferences((IProductCmptReference)parentElement)));
        // }
        //
        // // if (!fShowAssociationType && parentElement instanceof IProductCmptReference) {
        // // childsForAssociationProductCmpts = structure
        // // .getChildProductCmptReferences((IProductCmptReference)parentElement);
        // // } else if (parentElement instanceof IProductCmptReference) {
        // // childsForAssociationProductCmpts = structure
        // // .getChildProductCmptTypeRelationReferences((IProductCmptReference)parentElement);
        // } else if (parentElement instanceof IProductCmptStructureReference) {
        // children.addAll(Arrays.asList(structure
        // .getChildProductCmptReferences((IProductCmptStructureReference)parentElement)));
        // }
        // ENDE: Entwicklungsstand SMART-MODE

        // add product cmpt associations and product cmpts
        if (!showAssociationNodes && parentElement instanceof IProductCmptReference parentProductCmptReference) {
            for (IProductCmptReference productCmptReference : structure
                    .getChildProductCmptReferences(parentProductCmptReference)) {
                IProductCmptTypeAssociationReference relationReference = productCmptReference.getParent();
                if (showAssociatedCmpts || relationReference == null
                        || !relationReference.getAssociation().isAssoziation()) {
                    children.add(productCmptReference);
                }
            }
        } else if (parentElement instanceof IProductCmptReference productCmptReference) {
            for (IProductCmptTypeAssociationReference relationReference : structure
                    .getChildProductCmptTypeAssociationReferences(productCmptReference)) {
                if (showAssociatedCmpts || !relationReference.getAssociation().isAssoziation()) {
                    children.add(relationReference);
                }
            }
        } else if (parentElement instanceof IProductCmptTypeAssociationReference associationReference) {
            children.addAll(Arrays.asList(structure.getChildProductCmptReferences(associationReference)));
        }

        if (showValidationRules && parentElement instanceof IProductCmptReference productCmptReference) {
            children.addAll(
                    Arrays.asList(structure.getChildProductCmptVRuleReferences(productCmptReference)));
        }
        // add table content usages
        if (showTableContents && parentElement instanceof IProductCmptReference productCmptReference) {
            children.addAll(Arrays.asList(
                    structure.getChildProductCmptStructureTblUsageReference(productCmptReference)));
        }

        return children.toArray();
    }

    @Override
    public Object getParent(Object element) {
        if (structure == null) {
            return null;
        }

        if (!showAssociationNodes && element instanceof IProductCmptReference productCmptReference) {
            return structure.getParentProductCmptReference(productCmptReference);
        } else if (element instanceof IProductCmptReference productCmptReference) {
            return structure.getParentProductCmptTypeRelationReference(productCmptReference);
        } else if (element instanceof IProductCmptStructureReference structureReference) {
            return structure.getParentProductCmptReference(structureReference);
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
            return new Object[] { structure.getRoot() };
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
        // root = structure.getRoot();
    }

    /**
     * Returns <code>true</code> if the association type will be displayed besides the related
     * product cmpt.
     *
     * @return true if association type showing is on
     */
    public boolean isAssociationTypeShowing() {
        return showAssociationNodes;
    }

    /**
     * Sets if the association type will be shown or hidden.
     *
     * @param showAssociationType set true for showing association type
     */
    public void setShowAssociationNodes(boolean showAssociationType) {
        showAssociationNodes = showAssociationType;
    }

    /**
     * Returns <code>true</code> if the related table contents cmpts will be shown or hidden.
     *
     * @return true if show table contents components are shown
     */
    public boolean isShowTableContents() {
        return showTableContents;
    }

    /**
     * Returns <code>true</code> if a product component's validation rules will be shown or hidden.
     *
     * @return true if show table contents components are shown
     */
    public boolean isShowValidationRules() {
        return showValidationRules;
    }

    /**
     * Set <code>true</code> to show related table contents.
     */
    public void setShowTableContents(boolean showTableContents) {
        this.showTableContents = showTableContents;
    }

    public void setShowAssociatedCmpts(boolean showAssociations) {
        showAssociatedCmpts = showAssociations;
    }

    /**
     * Set <code>true</code> to show a product components validation rules.
     */
    public void setShowValidationRules(boolean showRules) {
        showValidationRules = showRules;
    }

    /**
     * Searches the product component structure for references to the product component defined in
     * the given source file. Returns <code>false</code> if errors occur when reading from the given
     * {@link IIpsSrcFile} or if it does not contain an {@link IProductCmpt}.
     *
     * @param ipsSrcFile the source file containing the product component.
     * @return <code>true</code> if the given product component is referenced by the structure,
     *             <code>false</code> otherwise.
     */
    public boolean isIpsSrcFilePartOfStructure(IIpsSrcFile ipsSrcFile) {
        if (ipsSrcFile != null && structure != null && ipsSrcFile.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT) {
            return structure.referencesProductCmptQualifiedName(ipsSrcFile.getQualifiedNameType().getName());
        }
        return false;
    }
}
