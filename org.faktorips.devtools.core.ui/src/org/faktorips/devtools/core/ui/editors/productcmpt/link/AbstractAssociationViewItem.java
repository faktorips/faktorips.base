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
import java.util.List;

import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;

/**
 * Represents an association in the product component editor's link section. Association items are
 * the root items in the tree. Their children are the corresponding links.
 * 
 * @author widmaier
 */
public abstract class AbstractAssociationViewItem implements ILinkSectionViewItem {

    private final IProductCmptLinkContainer linkContainer;

    protected AbstractAssociationViewItem(IProductCmptLinkContainer container) {
        linkContainer = container;
    }

    /**
     * Returns the link items for this association item.
     */
    public List<ILinkSectionViewItem> getChildren() {
        List<ILinkSectionViewItem> items = new ArrayList<>();
        List<IProductCmptLink> links = getLinkContainer().getLinksAsList(getAssociationName());
        for (IProductCmptLink link : links) {
            items.add(new LinkViewItem(link));
        }
        return items;
    }

    public boolean hasChildren() {
        return !getLinkContainer().getLinksAsList(getAssociationName()).isEmpty();
    }

    public IProductCmptLinkContainer getLinkContainer() {
        return linkContainer;
    }

    /**
     * Returns the product component of the {@link IProductCmptLinkContainer}. This may be the link
     * container itself or in case of the link container is a generation it is the generation's
     * product component.
     * 
     * @return The product component associated with this view item
     */
    public IProductCmpt getProductCmpt() {
        return linkContainer.getProductCmpt();
    }

}
