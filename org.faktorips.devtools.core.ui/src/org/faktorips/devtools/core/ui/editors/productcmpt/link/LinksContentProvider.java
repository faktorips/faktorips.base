/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.ui.util.LinkCreatorUtil;

/**
 * Provides the content for a generation-based association-tree. The association names are requested
 * from the given generation and all supertypes the type containing this generation is based on.
 * 
 * @author Thorsten Guenther
 */
public class LinksContentProvider implements ITreeContentProvider {

    @Override
    public Object[] getElements(Object inputElement) {
        if (!(inputElement instanceof IProductCmptGeneration)) {
            throw new RuntimeException("Unknown input element type " + inputElement.getClass()); //$NON-NLS-1$
        }
        IProductCmptGeneration generation = (IProductCmptGeneration)inputElement;
        try {
            IProductCmpt pc = generation.getProductCmpt();

            IProductCmptType pcType = pc.findProductCmptType(generation.getIpsProject());
            if (pcType == null) {
                /*
                 * Type can't be found in case product component is loaded from a VCS repository.
                 * Extract the association names from the links in the generation and product
                 * component.
                 */
                return getDetachedAssociationViewItems(generation);
            } else {
                // find association using the product cmpt's project
                return getAssociationItems(pcType, pc.getIpsProject(), generation);
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    protected DetachedAssociationViewItem[] getDetachedAssociationViewItems(IProductCmptGeneration gen) {
        List<DetachedAssociationViewItem> items = new ArrayList<DetachedAssociationViewItem>();
        items.addAll(getAssociationItemsForLinkContainer(gen.getProductCmpt()));
        items.addAll(getAssociationItemsForLinkContainer(gen));
        return items.toArray(new DetachedAssociationViewItem[items.size()]);
    }

    protected List<DetachedAssociationViewItem> getAssociationItemsForLinkContainer(IProductCmptLinkContainer linkContainer) {
        List<DetachedAssociationViewItem> items = new ArrayList<DetachedAssociationViewItem>();
        Set<String> associations = new LinkedHashSet<String>();
        List<IProductCmptLink> links = linkContainer.getLinksAsList();
        for (IProductCmptLink link : links) {
            if (associations.add(link.getAssociation())) {
                items.add(new DetachedAssociationViewItem(linkContainer, link.getAssociation()));
            }
        }
        return items;
    }

    protected AssociationViewItem[] getAssociationItems(IProductCmptType type,
            IIpsProject ipsProject,
            IProductCmptGeneration generation) {
        List<AssociationViewItem> items = new ArrayList<AssociationViewItem>();
        List<IProductCmptTypeAssociation> associations = type.findAllNotDerivedAssociations(ipsProject);
        for (IProductCmptTypeAssociation association : associations) {
            if (association.isRelevant()) {
                IProductCmptLinkContainer container = LinkCreatorUtil.getLinkContainerFor(generation, association);
                AssociationViewItem associationViewItem = new AssociationViewItem(container, association);
                items.add(associationViewItem);
            }
        }
        return items.toArray(new AssociationViewItem[items.size()]);
    }

    @Override
    public void dispose() {
        // Nothing to do
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        /*
         * No need to implement. Input is given in #getElements() and needs to be recalculated every
         * time anyways.
         */
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof AbstractAssociationViewItem) {
            List<ILinkSectionViewItem> children = ((AbstractAssociationViewItem)parentElement).getChildren();
            return children.toArray(new ILinkSectionViewItem[children.size()]);
        }
        return new Object[0];
    }

    @Override
    public Object getParent(Object element) {
        /*
         * No need to implement. Would be needed if a single item had to be expanded in the tree.
         */
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        return getChildren(element).length > 0;
    }

}
