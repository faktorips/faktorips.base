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
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsObjectPartDecorator;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IValueHolder;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;

public class AttributeValueDecorator implements IIpsObjectPartDecorator {

    public static final String PRODUCT_ATTRIBUTE_ICON = "ProductAttribute.gif"; //$NON-NLS-1$

    @Override
    public ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        return getDefaultImageDescriptor();
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IIpsDecorators.getImageHandling().createImageDescriptor(PRODUCT_ATTRIBUTE_ICON);
    }

    @Override
    public String getLabel(IIpsObjectPart ipsObjectPart) {
        if (!(ipsObjectPart instanceof IAttributeValue)) {
            return IIpsObjectPartDecorator.super.getLabel(ipsObjectPart);
        }

        IAttributeValue attributeValue = (IAttributeValue)ipsObjectPart;

        String caption = IIpsModel.get().getMultiLanguageSupport().getLocalizedCaption(attributeValue);

        IValueHolder<?> valueHolder = attributeValue.getValueHolder();
        String value = valueHolder != null ? valueHolder.getStringValue() : null;
        // try to get formatted value
        IProductCmptTypeAttribute attribute = attributeValue.findAttribute(attributeValue.getIpsProject());
        if (attribute != null) {
            value = IIpsModelExtensions.get().getModelPreferences().getDatatypeFormatter()
                    .formatValue(attribute.findDatatype(attributeValue.getIpsProject()), value);
        }
        return caption + ": " + value; //$NON-NLS-1$
    }
}
