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
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.runtime.internal.IpsStringUtils;

public class AttributeDecorator implements IIpsObjectPartDecorator {

    public static final String PUBLISHED_BASE_IMAGE = "AttributePublished.gif"; //$NON-NLS-1$

    public static final String PUBLIC_BASE_IMAGE = "AttributePublic.gif"; //$NON-NLS-1$

    @Override
    public ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {

        if (ipsObjectPart instanceof IAttribute) {
            IAttribute attribute = (IAttribute)ipsObjectPart;

            String baseImage = attribute.getModifier().isPublished() ? PUBLISHED_BASE_IMAGE : PUBLIC_BASE_IMAGE;

            String[] overlays = new String[4];

            if (attribute instanceof IProductCmptTypeAttribute && !attribute.isChangingOverTime()) {
                overlays[0] = OverlayIcons.STATIC;
            }

            if (attribute instanceof IPolicyCmptTypeAttribute
                    && ((IPolicyCmptTypeAttribute)attribute).isProductRelevant()) {
                overlays[IDecoration.TOP_RIGHT] = OverlayIcons.PRODUCT_RELEVANT;

                if (!attribute.isChangingOverTime()) {
                    overlays[IDecoration.TOP_LEFT] = OverlayIcons.STATIC;
                }
            }
            if (attribute.isOverwrite()) {
                overlays[IDecoration.BOTTOM_RIGHT] = OverlayIcons.OVERRIDE;
            }
            if (attribute.isDeprecated()) {
                overlays[IDecoration.BOTTOM_LEFT] = OverlayIcons.DEPRECATED;
            }

            return IIpsDecorators.getImageHandling().getSharedOverlayImageDescriptor(baseImage, overlays);
        }
        return getDefaultImageDescriptor();
    }

    @Override
    public String getLabel(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IAttribute) {
            IAttribute attribute = (IAttribute)ipsObjectPart;
            String label = attribute.getName();
            if (attribute.isDerived()) {
                label = "/ " + label; //$NON-NLS-1$
            }
            if (!IpsStringUtils.isEmpty(attribute.getDatatype())) {
                label += " : " + attribute.getDatatype(); //$NON-NLS-1$
            }
            return label;
        } else {
            return IIpsObjectPartDecorator.super.getLabel(ipsObjectPart);
        }
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IIpsDecorators.getImageHandling().getSharedImageDescriptor(PUBLISHED_BASE_IMAGE, true);
    }
}
