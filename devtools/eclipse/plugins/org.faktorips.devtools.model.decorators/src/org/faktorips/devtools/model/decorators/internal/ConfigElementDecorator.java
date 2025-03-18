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
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsObjectPartDecorator;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.productcmpt.IConfigElement;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;

public class ConfigElementDecorator implements IIpsObjectPartDecorator {

    public static final String CONFIG_ELEMENT_ICON = "ConfigElement.gif"; //$NON-NLS-1$
    public static final String CONFIGURED_DEFAULT_ICON = "ConfiguredDefault.gif"; //$NON-NLS-1$
    public static final String CONFIGURED_VALUE_SET_ICON = "ConfiguredValueSet.gif"; //$NON-NLS-1$

    @Override
    public ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        return switch (ipsObjectPart) {
            case IConfiguredDefault $ -> getConfiguredDefaultImageDescriptor();
            case IConfiguredValueSet $ -> getConfiguredValueSetImageDescriptor();
            case null, default -> getDefaultImageDescriptor();
        };
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IIpsDecorators.getImageHandling().createImageDescriptor(CONFIG_ELEMENT_ICON);
    }

    public ImageDescriptor getConfiguredDefaultImageDescriptor() {
        return IIpsDecorators.getImageHandling().createImageDescriptor(CONFIGURED_DEFAULT_ICON);
    }

    public ImageDescriptor getConfiguredValueSetImageDescriptor() {
        return IIpsDecorators.getImageHandling().createImageDescriptor(CONFIGURED_VALUE_SET_ICON);
    }

    @Override
    public String getLabel(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IConfigElement configElement) {
            String caption = IIpsModel.get().getMultiLanguageSupport().getLocalizedCaption(configElement);
            Object value = configElement.getPropertyValue();
            if (value instanceof String) {
                ValueDatatype datatype = configElement.findValueDatatype(ipsObjectPart.getIpsProject());
                if (datatype != null) {
                    value = IIpsModelExtensions.get().getModelPreferences().getDatatypeFormatter()
                            .formatValue(datatype, (String)value);
                }
                return caption + ": " + value; //$NON-NLS-1$
            } else {
                return caption;
            }
        } else {
            return IIpsObjectPartDecorator.super.getLabel(ipsObjectPart);
        }
    }
}
