/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;

public class AttributeWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {

    public static final String PUBLISHED_BASE_IMAGE = "AttributePublished.gif"; //$NON-NLS-1$

    public static final String PUBLIC_BASE_IMAGE = "AttributePublic.gif"; //$NON-NLS-1$

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IAttribute) {
            IAttribute attribute = (IAttribute)ipsObjectPart;
            String baseImage = PUBLISHED_BASE_IMAGE;
            if (attribute.getModifier().isPublic()) {
                baseImage = PUBLIC_BASE_IMAGE;
            }
            String[] overlays = new String[4];

            if (attribute instanceof IPolicyCmptTypeAttribute
                    && ((IPolicyCmptTypeAttribute)attribute).isProductRelevant()) {
                overlays[1] = OverlayIcons.PRODUCT_OVR;
            }
            if (attribute instanceof IProductCmptTypeAttribute
                    && !((IProductCmptTypeAttribute)attribute).isChangingOverTime()) {
                overlays[0] = OverlayIcons.NOT_CHANGEOVERTIME_OVR;
            }
            if (attribute.isOverwrite()) {
                overlays[3] = OverlayIcons.OVERRIDE_OVR;
            }
            return IpsUIPlugin.getImageHandling().getSharedOverlayImage(baseImage, overlays);
        }
        return getDefaultImageDescriptor();
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().getSharedImageDescriptor(PUBLISHED_BASE_IMAGE, true);
    }

    @Override
    protected String getLabel(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IAttribute) {
            IAttribute attribute = (IAttribute)ipsObjectPart;
            String label = attribute.getName();
            if (attribute.isDerived()) {
                label = "/ " + label; //$NON-NLS-1$
            }
            if (!StringUtils.isEmpty(attribute.getDatatype())) {
                label += " : " + attribute.getDatatype(); //$NON-NLS-1$
            }
            return label;
        } else {
            return super.getLabel(ipsObjectPart);
        }
    }

}
