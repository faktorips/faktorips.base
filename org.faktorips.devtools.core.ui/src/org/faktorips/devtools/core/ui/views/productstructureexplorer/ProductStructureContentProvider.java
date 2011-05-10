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

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;

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
        List<IProductCmptStructureReference> children = new ArrayList<IProductCmptStructureReference>();

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
        if (!showAssociationNodes && parentElement instanceof IProductCmptReference) {
            // Arrays.asLists returns an AbstractList that could not be modified
            List<IProductCmptReference> list = new ArrayList<IProductCmptReference>(Arrays.asList(structure
                    .getChildProductCmptReferences((IProductCmptReference)parentElement)));
            if (!showAssociatedCmpts) {
                // filter association nodes
                for (Iterator<IProductCmptReference> iterator = list.iterator(); iterator.hasNext();) {
                    IProductCmptReference aProductCmptReference = iterator.next();
                    if (aProductCmptReference.getParent() instanceof IProductCmptTypeAssociationReference) {
                        IProductCmptTypeAssociationReference relationReference = (IProductCmptTypeAssociationReference)aProductCmptReference
                                .getParent();
                        if (relationReference.getAssociation().isAssoziation()) {
                            iterator.remove();
                        }
                    }
                }
            }
            children.addAll(list);
        } else if (parentElement instanceof IProductCmptReference) {
            // Arrays.asLists returns an AbstractList that could not be modified
            List<IProductCmptTypeAssociationReference> list = new ArrayList<IProductCmptTypeAssociationReference>(
                    Arrays.asList(structure
                            .getChildProductCmptTypeAssociationReferences((IProductCmptReference)parentElement)));
            if (!showAssociatedCmpts) {
                // filter association nodes
                for (Iterator<IProductCmptTypeAssociationReference> iterator = list.iterator(); iterator.hasNext();) {
                    IProductCmptTypeAssociationReference aRelationReference = iterator.next();
                    if (aRelationReference.getAssociation().isAssoziation()) {
                        iterator.remove();
                    }

                }
            }
            children.addAll(list);
        } else if (parentElement instanceof IProductCmptTypeAssociationReference) {
            List<IProductCmptReference> list = Arrays.asList(structure
                    .getChildProductCmptReferences((IProductCmptTypeAssociationReference)parentElement));
            children.addAll(list);
        }

        if (showValidationRules && parentElement instanceof IProductCmptReference) {
            children.addAll(Arrays.asList(structure
                    .getChildProductCmptVRuleReferences((IProductCmptReference)parentElement)));
        }
        // add table content usages
        if (showTableContents && parentElement instanceof IProductCmptReference) {
            children.addAll(Arrays.asList(structure
                    .getChildProductCmptStructureTblUsageReference((IProductCmptReference)parentElement)));
        }

        return children.toArray();
    }

    @Override
    public Object getParent(Object element) {
        if (structure == null) {
            return null;
        }

        if (!showAssociationNodes && element instanceof IProductCmptReference) {
            return structure.getParentProductCmptReference((IProductCmptReference)element);
        } else if (element instanceof IProductCmptReference) {
            return structure.getParentProductCmptTypeRelationReference((IProductCmptReference)element);
        } else if (element instanceof IProductCmptStructureReference) {
            return structure.getParentProductCmptReference((IProductCmptStructureReference)element);
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
     *         <code>false</code> otherwise.
     */
    public boolean isIpsSrcFilePartOfStructure(IIpsSrcFile ipsSrcFile) {
        if (ipsSrcFile != null && structure != null && ipsSrcFile.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT) {
            return structure.referencesProductCmptQualifiedName(ipsSrcFile.getQualifiedNameType().getName());
        }
        return false;
    }
}
