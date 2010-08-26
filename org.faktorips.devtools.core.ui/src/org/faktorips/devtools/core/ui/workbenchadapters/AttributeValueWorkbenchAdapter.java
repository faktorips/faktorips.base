/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;

public class AttributeValueWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        // As of now, attribute values do not have an image.
        return null;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        // As of now, attribute values do not have an image.
        return null;
    }

    @Override
    protected String getLabel(IIpsObjectPart ipsObjectPart) {
        if (!(ipsObjectPart instanceof IAttributeValue)) {
            return super.getLabel(ipsObjectPart);
        }

        IAttributeValue attributeValue = (IAttributeValue)ipsObjectPart;
        return attributeValue.getCurrentLabel();
    }

}
