/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin.ImageHandling;

public class ProductCmptLinkWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {

    private static final String OVERLAY_GIF = "LinkOverlay.gif"; //$NON-NLS-1$

    private static final String PRODUCT_CMPT_LINK_GIF = "ProductCmptLink.gif"; //$NON-NLS-1$

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IProductCmptLink) {
            IProductCmptLink link = (IProductCmptLink)ipsObjectPart;
            try {
                IProductCmpt findTarget = link.findTarget(ipsObjectPart.getIpsProject());
                ImageHandling imageHandling = IpsUIPlugin.getImageHandling();
                ImageDescriptor imageDescTarget = imageHandling.getImageDescriptor(findTarget);
                return imageHandling.getSharedOverlayImageDescriptor(imageDescTarget.createImage(), OVERLAY_GIF,
                        IDecoration.BOTTOM_LEFT);
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return getDefaultImageDescriptor();
            }
        }
        return null;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().getSharedImageDescriptor(PRODUCT_CMPT_LINK_GIF, true);
    }

    @Override
    protected String getLabel(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IProductCmptLink) {
            IProductCmptLink link = (IProductCmptLink)ipsObjectPart;
            try {
                IProductCmpt findTarget = link.findTarget(ipsObjectPart.getIpsProject());
                if (findTarget == null) {
                    return StringUtils.EMPTY;
                }
                return findTarget.getName();
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        return super.getLabel(ipsObjectPart);
    }

}
