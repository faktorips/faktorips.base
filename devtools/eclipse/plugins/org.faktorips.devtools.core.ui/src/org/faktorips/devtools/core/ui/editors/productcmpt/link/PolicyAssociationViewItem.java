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
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.decorators.internal.AssociationDecorator;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;

/**
 * Represents a policy component type association in the product component editor's structure section.
 * Used for policy associations that have  a configured cardinality but are not matched by
 * any product component type association.
 */
public class PolicyAssociationViewItem extends AbstractAssociationViewItem {

    private final IPolicyCmptTypeAssociation association;
    private final AssociationDecorator workbenchAdapter;

    public PolicyAssociationViewItem(IProductCmptLinkContainer container,
            IPolicyCmptTypeAssociation association) {
        super(container);
        this.association = association;
        workbenchAdapter = new AssociationDecorator(false);
    }

    public IPolicyCmptTypeAssociation getAssociation() {
        return association;
    }

    @Override
    public String getAssociationName() {
        return association.getName();
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
    public boolean hasChildren() {
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(association);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        PolicyAssociationViewItem other = (PolicyAssociationViewItem)obj;
        return Objects.equals(association, other.association);
    }

}
