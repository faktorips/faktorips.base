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
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;

public class ProductCmptTypeMethodDecorator extends MethodDecorator {

    /**
     * Returns an adequate {@link ImageDescriptor}. If the given {@link IIpsObjectPart} is an
     * {@link IProductCmptTypeMethod} with a static method the returned {@link ImageDescriptor} will
     * contain an overlaid icon.
     */
    @Override
    public ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IProductCmptTypeMethod) {
            IProductCmptTypeMethod method = (IProductCmptTypeMethod)ipsObjectPart;
            String[] overlays = getOverlays(method);
            if (!method.isChangingOverTime()) {
                overlays[0] = OverlayIcons.STATIC;
            }
            return IIpsDecorators.getImageHandling().getSharedOverlayImageDescriptor(METHOD_IMAGE_NAME, overlays);
        } else {
            return getDefaultImageDescriptor();
        }
    }
}
