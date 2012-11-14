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

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * Item represents an association in the product component editor's link section. Holds a reference
 * to the model, e.g. the product component type association to be represented.
 * 
 * @author widmaier
 */
public class AssociationViewItem extends AbstractAssociationViewItem {

    private final IProductCmptTypeAssociation association;

    protected AssociationViewItem(IProductCmptLinkContainer container, IProductCmptTypeAssociation association) {
        super(container);
        this.association = association;
    }

    @Override
    public String getText() {
        if (association.is1ToMany()) {
            return IpsPlugin.getMultiLanguageSupport().getLocalizedPluralLabel(association);
        } else {
            return IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(association);
        }
    }

    @Override
    public Image getImage() {
        return IpsUIPlugin.getImageHandling().getImage(association);
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
