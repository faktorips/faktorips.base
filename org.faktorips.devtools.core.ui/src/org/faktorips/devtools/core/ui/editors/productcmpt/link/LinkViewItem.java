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
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.StringUtil;

public class LinkViewItem implements LinkSectionViewItem {

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

}
