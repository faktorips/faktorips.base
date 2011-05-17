/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class AttributeValueWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IAttributeValue) {
            IAttributeValue attributeValue = (IAttributeValue)ipsObjectPart;
            try {
                IProductCmptTypeAttribute attribute = attributeValue.findAttribute(attributeValue.getIpsProject());
                if (attribute != null) {
                    return IpsUIPlugin.getImageHandling().getImageDescriptor(attribute);
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
            return getDefaultImageDescriptor();
        } else {
            return null;
        }
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().getDefaultImageDescriptor(ProductCmptTypeAttribute.class);
    }

    @Override
    protected String getLabel(IIpsObjectPart ipsObjectPart) {
        if (!(ipsObjectPart instanceof IAttributeValue)) {
            return super.getLabel(ipsObjectPart);
        }

        IAttributeValue attributeValue = (IAttributeValue)ipsObjectPart;

        String caption = IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(attributeValue);

        String value = attributeValue.getValue();
        try {
            // try to get formatted value
            IProductCmptTypeAttribute attribute = attributeValue.findAttribute(attributeValue.getIpsProject());
            if (attribute != null) {
                value = IpsUIPlugin.getDefault().getDatatypeFormatter()
                        .formatValue(attribute.findDatatype(attributeValue.getIpsProject()), attributeValue.getValue());
            }
        } catch (CoreException e) {
            // ignore exceptions because we log a bunch of these if there is any
            // the value is also displayed unformatted
        }
        return caption + ": " + value; //$NON-NLS-1$
    }
}
