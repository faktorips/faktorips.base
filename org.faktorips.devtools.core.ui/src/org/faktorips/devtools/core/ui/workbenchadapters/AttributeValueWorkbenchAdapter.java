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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.type.IAttribute;

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
        if (ipsObjectPart instanceof IAttributeValue) {
            IAttributeValue attributeValue = (IAttributeValue)ipsObjectPart;
            try {
                IAttribute attribute = attributeValue.findAttribute(ipsObjectPart.getIpsProject());
                if (attribute == null) {
                    IpsPlugin
                            .log(new IpsStatus(
                                    IStatus.WARNING,
                                    "Could not find the attribute the attribute value '" + attributeValue.getName() + "' is based on.")); //$NON-NLS-1$ //$NON-NLS-2$
                    return ""; //$NON-NLS-1$
                }
                // TODO AW: ! attribute.getCurrentLabel();
                return attribute.getName();
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }
        return super.getLabel(ipsObjectPart);
    }

}
