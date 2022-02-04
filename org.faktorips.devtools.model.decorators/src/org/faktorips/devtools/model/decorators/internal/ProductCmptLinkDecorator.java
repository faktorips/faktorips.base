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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsObjectPartDecorator;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.runtime.internal.IpsStringUtils;

public class ProductCmptLinkDecorator implements IIpsObjectPartDecorator {

    public static final String PRODUCT_CMPT_LINK_IMAGE = "ProductCmptLink.gif"; //$NON-NLS-1$

    @Override
    public ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IProductCmptLink) {
            IProductCmptLink link = (IProductCmptLink)ipsObjectPart;
            try {
                IProductCmpt target = link.findTarget(ipsObjectPart.getIpsProject());
                return IIpsDecorators.getImageHandling().getSharedOverlayImageDescriptor(
                        IIpsDecorators.getImageDescriptor(target),
                        OverlayIcons.LINK,
                        IDecoration.BOTTOM_RIGHT);
            } catch (CoreRuntimeException e) {
                IpsLog.log(e);
            }
        }
        return getDefaultImageDescriptor();
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IIpsDecorators.getImageHandling().getSharedImageDescriptor(PRODUCT_CMPT_LINK_IMAGE, true);
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
            } catch (CoreRuntimeException e) {
                IpsLog.log(e);
            }
        }
        return IIpsObjectPartDecorator.super.getLabel(ipsObjectPart);
    }
}
