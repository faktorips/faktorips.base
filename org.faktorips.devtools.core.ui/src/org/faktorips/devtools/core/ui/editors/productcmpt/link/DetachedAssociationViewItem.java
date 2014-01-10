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

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.workbenchadapters.AssociationWorkbenchAdapter;

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
        return IpsUIPlugin.getImageHandling().getImage(new AssociationWorkbenchAdapter().getDefaultImageDescriptor());
    }

    @Override
    public String getAssociationName() {
        return associationName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((associationName == null) ? 0 : associationName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DetachedAssociationViewItem other = (DetachedAssociationViewItem)obj;
        if (associationName == null) {
            if (other.associationName != null) {
                return false;
            }
        } else if (!associationName.equals(other.associationName)) {
            return false;
        }
        return true;
    }

}
