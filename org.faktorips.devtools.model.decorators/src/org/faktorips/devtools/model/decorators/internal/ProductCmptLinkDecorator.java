/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.decorators.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsObjectPartDecorator;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.runtime.internal.IpsStringUtils;

public class ProductCmptLinkDecorator implements IIpsObjectPartDecorator {

    private static final String OVERLAY_GIF = "overlays/LinkOverlay.gif"; //$NON-NLS-1$
    private static final String PRODUCT_CMPT_LINK_GIF = "ProductCmptLink.gif"; //$NON-NLS-1$

    @Override
    public ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IProductCmptLink) {
            IProductCmptLink link = (IProductCmptLink)ipsObjectPart;
            try {
                IProductCmpt target = link.findTarget(ipsObjectPart.getIpsProject());
                return IIpsDecorators.getImageHandling().getSharedOverlayImageDescriptor(
                        IIpsDecorators.getImageHandling().getImage(target),
                        OVERLAY_GIF,
                        IDecoration.BOTTOM_RIGHT);
            } catch (CoreException e) {
                IpsLog.log(e);
                return getDefaultImageDescriptor();
            }
        }
        return null;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsDecorators.getImageHandling().getSharedImageDescriptor(PRODUCT_CMPT_LINK_GIF, true);
    }

    @Override
    public String getLabel(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IProductCmptLink) {
            IProductCmptLink link = (IProductCmptLink)ipsObjectPart;
            try {
                IProductCmpt findTarget = link.findTarget(ipsObjectPart.getIpsProject());
                if (findTarget == null) {
                    return IpsStringUtils.EMPTY;
                }
                return findTarget.getName();
            } catch (CoreException e) {
                IpsLog.log(e);
            }
        }
        return IIpsObjectPartDecorator.super.getLabel(ipsObjectPart);
    }
}
