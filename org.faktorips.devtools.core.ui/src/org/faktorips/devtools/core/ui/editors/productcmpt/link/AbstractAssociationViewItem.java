/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.internal.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;

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
        List<ILinkSectionViewItem> items = new ArrayList<ILinkSectionViewItem>();
        List<IProductCmptLink> links = getLinkContainer().getLinksAsList(getAssociationName());
        for (IProductCmptLink link : links) {
            items.add(new LinkViewItem(link));
        }
        return items;
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
