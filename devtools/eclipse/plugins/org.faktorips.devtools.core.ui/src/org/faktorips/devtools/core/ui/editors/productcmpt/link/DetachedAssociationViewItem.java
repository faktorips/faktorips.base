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

import java.util.Objects;

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.decorators.internal.AssociationDecorator;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;

/**
 * Represents a product component type association without holding a reference to the association or
 * its product component type. When Product components are loaded from a VCS repository no model
 * (e.g. product component type) is available. To display associations and links anyway this item
 * must be used.
 * 
 * @author widmaier
 */
public class DetachedAssociationViewItem extends AbstractAssociationViewItem {

    private final String associationName;

    public DetachedAssociationViewItem(IProductCmptLinkContainer linkContainer, String assocName) {
        super(linkContainer);
        associationName = assocName;
    }

    @Override
    public String getText() {
        return associationName;
    }

    @Override
    public Image getImage() {
        return IpsUIPlugin.getImageHandling().getImage(new AssociationDecorator().getDefaultImageDescriptor());
    }

    @Override
    public String getAssociationName() {
        return associationName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(associationName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        DetachedAssociationViewItem other = (DetachedAssociationViewItem)obj;
        return Objects.equals(associationName, other.associationName);
    }

}
