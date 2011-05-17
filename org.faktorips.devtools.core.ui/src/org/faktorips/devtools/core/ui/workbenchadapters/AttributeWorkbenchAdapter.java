/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
                    && ((IProductCmptTypeAttribute)attribute).isChangingOverTime()) {
                overlays[0] = OverlayIcons.CHANGEOVERTIME_OVR;
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
