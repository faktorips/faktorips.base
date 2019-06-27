/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class AttributeValueWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IAttributeValue) {
            return getDefaultImageDescriptor();
        } else {
            return null;
        }
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().createImageDescriptor("ProductAttribute.gif"); //$NON-NLS-1$
    }

    @Override
    protected String getLabel(IIpsObjectPart ipsObjectPart) {
        if (!(ipsObjectPart instanceof IAttributeValue)) {
            return super.getLabel(ipsObjectPart);
        }

        IAttributeValue attributeValue = (IAttributeValue)ipsObjectPart;

        String caption = IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(attributeValue);

        IValueHolder<?> valueHolder = attributeValue.getValueHolder();
        String value = valueHolder != null ? valueHolder.getStringValue() : null;
        // try to get formatted value
        IProductCmptTypeAttribute attribute = attributeValue.findAttribute(attributeValue.getIpsProject());
        if (attribute != null) {
            value = IpsUIPlugin.getDefault().getDatatypeFormatter()
                    .formatValue(attribute.findDatatype(attributeValue.getIpsProject()), value);
        }
        return caption + ": " + value; //$NON-NLS-1$
    }
}
