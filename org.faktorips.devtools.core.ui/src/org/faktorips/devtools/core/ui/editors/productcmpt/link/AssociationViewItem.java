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

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.decorators.internal.AssociationDecorator;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * Item represents an association in the product component editor's link section. Holds a reference
 * to the model, e.g. the product component type association to be represented.
 * 
 * @author widmaier
 */
public class AssociationViewItem extends AbstractAssociationViewItem {

    private final IProductCmptTypeAssociation association;
    private final AssociationDecorator workbenchAdapter;

    protected AssociationViewItem(IProductCmptLinkContainer container, IProductCmptTypeAssociation association) {
        super(container);
        this.association = association;
        workbenchAdapter = new AssociationDecorator(association.getProductCmptType().isChangingOverTime());
    }

    @Override
    public String getText() {
        if (association.is1ToMany()) {
            return IIpsModel.get().getMultiLanguageSupport().getLocalizedPluralLabel(association);
        } else {
            return IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(association);
        }
    }

    @Override
    public Image getImage() {
        return IpsUIPlugin.getImageHandling().getImage(workbenchAdapter.getImageDescriptor(association));
    }

    @Override
    public String getAssociationName() {
        return association.getName();
    }

    public IProductCmptTypeAssociation getAssociation() {
        return association;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((association == null) ? 0 : association.hashCode());
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
        AssociationViewItem other = (AssociationViewItem)obj;
        if (association == null) {
            if (other.association != null) {
                return false;
            }
        } else if (!association.equals(other.association)) {
            return false;
        }
        return true;
    }

}
