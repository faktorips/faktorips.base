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

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;

public class ProductCmptTypeMethodWorkbenchAdapter extends MethodWorkbenchAdapter {

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IProductCmptTypeMethod) {
            IProductCmptTypeMethod method = (IProductCmptTypeMethod)ipsObjectPart;
            String[] overlays = getOverlays(method);
            if (!method.isChangingOverTime()) {
                overlays[0] = OverlayIcons.NOT_CHANGEOVERTIME_OVR;
            }
            return IpsUIPlugin.getImageHandling().getSharedOverlayImage(METHOD_IMAGE_NAME, overlays);
        } else {
            return getDefaultImageDescriptor();
        }
    }
}
