/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.internal.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;

/**
 * Represents an association in the product component editor's link section. Association items are
 * the root items in the tree. Their children are the corresponding links.
 * 
 * @author widmaier
 */
public abstract class AbstractAssociationViewItem implements LinkSectionViewItem {

    private final IProductCmptLinkContainer linkContainer;

    protected AbstractAssociationViewItem(IProductCmptLinkContainer container) {
        linkContainer = container;
    }

    /**
     * Returns the link items for this association item.
     */
    public List<LinkSectionViewItem> getChildren() {
        List<LinkSectionViewItem> items = new ArrayList<LinkSectionViewItem>();
        List<IProductCmptLink> links = getLinkContainer().getLinksAsList(getAssociationName());
        for (IProductCmptLink link : links) {
            items.add(new LinkViewItem(link));
        }
        return items;
    }

    public abstract String getAssociationName();

    public IProductCmptLinkContainer getLinkContainer() {
        return linkContainer;
    }

}
