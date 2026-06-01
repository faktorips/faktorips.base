/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.ui.util.LinkCreatorUtil;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * Provides the content for a generation-based association-tree. The association names are requested
 * from the given generation and all supertypes the type containing this generation is based on.
 *
 * @author Thorsten Guenther
 */
public class LinksContentProvider implements ITreeContentProvider {

    @Override
    public Object[] getElements(Object inputElement) {
        if (!(inputElement instanceof IProductCmptGeneration generation)) {
            throw new RuntimeException("Unknown input element type " + inputElement.getClass()); //$NON-NLS-1$
        }
        IProductCmpt pc = generation.getProductCmpt();

        IProductCmptType pcType = pc.findProductCmptType(generation.getIpsProject());
        if (pcType == null) {
            return getDetachedAssociationViewItems(generation);
        } else {
            return getAssociationItems(pcType, pc.getIpsProject(), generation);
        }
    }

    /**
     * Type can't be found in case product component is loaded from a VCS repository. Extract the
     * association names from the links in the generation and product component.
     */
    protected DetachedAssociationViewItem[] getDetachedAssociationViewItems(IProductCmptGeneration gen) {
        List<DetachedAssociationViewItem> items = new ArrayList<>();
        items.addAll(getAssociationItemsForLinkContainer(gen.getProductCmpt()));
        items.addAll(getAssociationItemsForLinkContainer(gen));
        return items.toArray(new DetachedAssociationViewItem[items.size()]);
    }

    /**
     * find association using the product cmpt's project
     */
    protected List<DetachedAssociationViewItem> getAssociationItemsForLinkContainer(
            IProductCmptLinkContainer linkContainer) {
        List<DetachedAssociationViewItem> items = new ArrayList<>();
        Set<String> associations = new LinkedHashSet<>();
        List<IProductCmptLink> links = linkContainer.getLinksAsList();
        for (IProductCmptLink link : links) {
            if (associations.add(link.getAssociation())) {
                items.add(new DetachedAssociationViewItem(linkContainer, link.getAssociation()));
            }
        }
        return items;
    }

    protected Object[] getAssociationItems(IProductCmptType type,
            IIpsProject ipsProject,
            IProductCmptGeneration generation) {
        List<AbstractAssociationViewItem> items = new ArrayList<>(buildProductItems(type, ipsProject, generation));
        items.addAll(buildPolicyItems(type, ipsProject, generation));
        return items.toArray();
    }

    private List<AbstractAssociationViewItem> buildProductItems(IProductCmptType type,
            IIpsProject ipsProject,
            IProductCmptGeneration generation) {
        List<AbstractAssociationViewItem> items = new ArrayList<>();
        for (IProductCmptTypeAssociation association : type.findAllNotDerivedAssociations(ipsProject)) {
            if (association.isRelevant()) {
                IProductCmptLinkContainer container = LinkCreatorUtil.getLinkContainerFor(generation, association);
                items.add(createAssociationViewItem(container, association));
            }
        }
        return items;
    }

    private List<AbstractAssociationViewItem> buildPolicyItems(IProductCmptType type,
            IIpsProject ipsProject,
            IProductCmptGeneration generation) {
        List<AbstractAssociationViewItem> items = new ArrayList<>();
        IPolicyCmptType policyCmptType = type.findPolicyCmptType(ipsProject);
        if (policyCmptType != null) {
            for (IAssociation assoc : policyCmptType.findAllAssociations(ipsProject)) {
                if (assoc instanceof IPolicyCmptTypeAssociation policyAssoc
                        && policyAssoc.isCardinalityConfigurable()
                        && policyAssoc.findMatchingProductCmptTypeAssociation(ipsProject) == null) {
                    items.add(new PolicyAssociationViewItem(generation, policyAssoc));
                }
            }
        }
        return items;
    }

    private AssociationViewItem createAssociationViewItem(IProductCmptLinkContainer container,
            IProductCmptTypeAssociation association) {
        return new AssociationViewItem(container, association);
    }

    @Override
    public void dispose() {
        // Nothing to do
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof AbstractAssociationViewItem assoc) {
            List<ILinkSectionViewItem> children = assoc.getChildren();
            return children.toArray(new ILinkSectionViewItem[0]);
        }
        return new Object[0];
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof LinkViewItem linkViewItem) {
            IProductCmptLink link = linkViewItem.getLink();
            IProductCmptTypeAssociation association = link.findAssociation(link.getIpsProject());
            return createAssociationViewItem(link.getProductCmptLinkContainer(), association);
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof AbstractAssociationViewItem assoc) {
            return assoc.hasChildren();
        }
        return false;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        /*
         * No need to implement. Input is given in #getElements() and needs to be recalculated every
         * time anyways.
         */
    }

}
