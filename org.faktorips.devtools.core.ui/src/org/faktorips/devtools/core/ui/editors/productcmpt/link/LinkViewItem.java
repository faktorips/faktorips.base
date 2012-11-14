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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.StringUtil;

/**
 * Represents a product component link in the product component editor's link section.
 * 
 * @author widmaier
 */
public class LinkViewItem extends PlatformObject implements ILinkSectionViewItem {

    private final IProductCmptLink link;

    public LinkViewItem(IProductCmptLink link) {
        this.link = link;
    }

    @Override
    public String getText() {
        return StringUtil.unqualifiedName(link.getTarget());
    }

    @Override
    public Image getImage() {
        IProductCmpt product;
        try {
            Image image;
            product = link.findTarget(link.getIpsProject());
            if (product == null) {
                image = IpsUIPlugin.getImageHandling().getDefaultImage(ProductCmpt.class);
            } else {
                image = IpsUIPlugin.getImageHandling().getImage(product);
            }
            return image;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public IProductCmptLink getLink() {
        return link;
    }

    @SuppressWarnings("rawtypes")
    // IAdaptable forces raw type upon implementing classes
    @Override
    public Object getAdapter(Class adapter) {
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    @Override
    public String getAssociationName() {
        return link.getAssociation();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((link == null) ? 0 : link.hashCode());
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
        LinkViewItem other = (LinkViewItem)obj;
        if (link == null) {
            if (other.link != null) {
                return false;
            }
        } else if (!link.equals(other.link)) {
            return false;
        }
        return true;
    }
}
