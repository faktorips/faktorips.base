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

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
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
        ImageDescriptor imageDescriptor;
        product = link.findTarget(link.getIpsProject());
        if (product == null) {
            imageDescriptor = IpsUIPlugin.getImageHandling().getDefaultImageDescriptor(ProductCmpt.class);
        } else {
            imageDescriptor = IpsUIPlugin.getImageHandling().getImageDescriptor(product);
        }
        if (link.getTemplateValueStatus() == TemplateValueStatus.UNDEFINED) {
            return IpsUIPlugin.getImageHandling().getDisabledSharedImage(imageDescriptor);
        } else {
            return IpsUIPlugin.getImageHandling().getImage(imageDescriptor);
        }
    }

    public IProductCmptLink getLink() {
        return link;
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    @Override
    public String getAssociationName() {
        return link.getAssociation();
    }

    @Override
    public int hashCode() {
        return Objects.hash(link);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        LinkViewItem other = (LinkViewItem)obj;
        return Objects.equals(link, other.link);
    }
}
