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

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;

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
    private boolean fShowAssociationType = true;

    // private IProductCmptReference root;

    private boolean showTableContents = true;

    /**
     * Creates a new content provider.
     * 
     * @param showAssociationType <code>true</code> to show the association types as nodes.
     */
    public ProductStructureContentProvider(boolean showAssociationType) {
        fShowAssociationType = showAssociationType;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getChildren(Object parentElement) {
        List<IProductCmptStructureReference> children = new ArrayList<IProductCmptStructureReference>();

        // TODO Entwicklungsstand SMART-MODE
        // // add product cmpt associations and product cmpts
        // if (parentElement instanceof IProductCmptReference) {
        // IProductCmptTypeRelationReference[] ralationReferences = structure
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

        IProductCmptStructureReference[] childsForAssociationProductCmpts = new IProductCmptStructureReference[0];
        // add product cmpt associations and product cmpts
        if (!fShowAssociationType && parentElement instanceof IProductCmptReference) {
            childsForAssociationProductCmpts = structure
                    .getChildProductCmptReferences((IProductCmptReference)parentElement);
        } else if (parentElement instanceof IProductCmptReference) {
            childsForAssociationProductCmpts = structure
                    .getChildProductCmptTypeRelationReferences((IProductCmptReference)parentElement);
        } else if (parentElement instanceof IProductCmptStructureReference) {
            childsForAssociationProductCmpts = structure
                    .getChildProductCmptReferences((IProductCmptStructureReference)parentElement);
        }
        children.addAll(Arrays.asList(childsForAssociationProductCmpts));

        // add table content usages
        if (showTableContents && parentElement instanceof IProductCmptReference) {
            children.addAll(Arrays.asList(structure
                    .getChildProductCmptStructureTblUsageReference((IProductCmptReference)parentElement)));
        }

        return children.toArray();
    }

    /**
     * {@inheritDoc}
     */
    public Object getParent(Object element) {
        if (structure == null) {
            return null;
        }

        if (!fShowAssociationType && element instanceof IProductCmptReference) {
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
            return new Object[] { structure.getRoot() };
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
        // root = structure.getRoot();
    }

    /**
     * Returns <code>true</code> if the association type will be displayed besides the related
     * product cmpt.
     * 
     * @return true if association type showing is on
     */
    public boolean isAssociationTypeShowing() {
        return fShowAssociationType;
    }

    /**
     * Sets if the association type will be shown or hidden.
     * 
     * @param showAssociationType set true for showing association type
     */
    public void setAssociationTypeShowing(boolean showAssociationType) {
        fShowAssociationType = showAssociationType;
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
     * Set <code>true</code> to show related table contents.
     * 
     * @param showTableContents
     */
    public void setShowTableContents(boolean showTableContents) {
        this.showTableContents = showTableContents;
    }
}
