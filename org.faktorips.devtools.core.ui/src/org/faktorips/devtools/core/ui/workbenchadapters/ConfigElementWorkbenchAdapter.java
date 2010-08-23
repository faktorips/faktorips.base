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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.type.IAttribute;

public class ConfigElementWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter implements
        IDescriptionWorkbenchAdapter {

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        // As of now configuration elements do not have an image.
        return null;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        // As of now configuration elements do not have an image.
        return null;
    }

    @Override
    protected String getLabel(IIpsObjectPart ipsObjectPart) {
        if (!(ipsObjectPart instanceof IConfigElement)) {
            return super.getLabel(ipsObjectPart);
        }

        IConfigElement configElement = (IConfigElement)ipsObjectPart;
        try {
            IAttribute attribute = configElement.findPcTypeAttribute(ipsObjectPart.getIpsProject());
            if (attribute == null) {
                IpsPlugin.log(new IpsStatus(IStatus.WARNING,
                        "Could not find the attribute the config element is based on.")); //$NON-NLS-1$ 
                return ""; //$NON-NLS-1$
            }

            String labelValue = getMostSuitableLabelValue(attribute, false);
            if (StringUtils.isEmpty(labelValue)) {
                return StringUtils.capitalize(attribute.getName());
            } else {
                return labelValue;
            }

        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDescription(IIpsElement ipsElement) {
        if (!(ipsElement instanceof IConfigElement)) {
            return ""; //$NON-NLS-1$
        }

        IConfigElement configElement = (IConfigElement)ipsElement;
        IAttribute attribute;
        try {
            attribute = configElement.findPcTypeAttribute(ipsElement.getIpsProject());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
        if (attribute == null) {
            IpsPlugin
                    .log(new IpsStatus(IStatus.WARNING, "Could not find the attribute the config element is based on.")); //$NON-NLS-1$ 
            return ""; //$NON-NLS-1$
        }

        return getMostSuitableDescriptionText(attribute);
    }

}
