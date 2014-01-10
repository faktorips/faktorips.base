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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;

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
        if (parentElement instanceof IProductCmptGeneration && element instanceof AbstractAssociationViewItem) {
            IProductCmptGeneration generation = (IProductCmptGeneration)parentElement;
            AbstractAssociationViewItem associationViewItem = (AbstractAssociationViewItem)element;
            IProductCmptLink[] links = generation.getLinks(associationViewItem.getAssociationName());
            return links.length != 0;
        }
        return true;
    }
}