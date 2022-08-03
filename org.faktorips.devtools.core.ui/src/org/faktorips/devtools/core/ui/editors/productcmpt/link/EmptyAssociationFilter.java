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

import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;

/**
 * This Class filters empty associations of an {@link IProductCmptGeneration}.
 * <p>
 * It works, when an association has an {@link IProductCmptGeneration} as parentElement in the
 * Viewer. The association must be represented by an {@link AbstractAssociationViewItem}.
 * </p>
 * 
 * @author dicker
 */
public class EmptyAssociationFilter extends ViewerFilter {

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (canSelect(element)) {
            List<IProductCmptLink> links = getLinks(element);
            return !links.isEmpty();
        }
        return true;
    }

    private List<IProductCmptLink> getLinks(Object element) {
        return getLinks((AbstractAssociationViewItem)element);
    }

    private List<IProductCmptLink> getLinks(AbstractAssociationViewItem associationViewItem) {
        IProductCmptLinkContainer linkContainer = associationViewItem.getLinkContainer();
        return linkContainer.getLinksAsList(associationViewItem.getAssociationName());
    }

    private boolean canSelect(Object element) {
        return element instanceof AbstractAssociationViewItem;
    }
}
