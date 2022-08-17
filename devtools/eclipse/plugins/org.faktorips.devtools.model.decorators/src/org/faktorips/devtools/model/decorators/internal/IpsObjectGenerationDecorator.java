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

import java.text.DateFormat;
import java.util.GregorianCalendar;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsObjectPartDecorator;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IChangesOverTimeNamingConvention;

public class IpsObjectGenerationDecorator implements IIpsObjectPartDecorator {

    private static final String GENERATION_IMAGE_BASE = "Generation"; //$NON-NLS-1$

    private IChangesOverTimeNamingConvention cachedNamingConvention;
    private ImageDescriptor cachedImageDescriptor;

    @Override
    public ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        // Image is independent of object
        return getDefaultImageDescriptor();
    }

    @Override
    public String getLabel(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IIpsObjectGeneration) {
            return getLabel((IIpsObjectGeneration)ipsObjectPart);
        } else {
            return IIpsObjectPartDecorator.super.getLabel(ipsObjectPart);
        }
    }

    private String getLabel(IIpsObjectGeneration ipsObjectGeneration) {
        GregorianCalendar validFrom = ipsObjectGeneration.getValidFrom();
        if (validFrom == null) {
            return IIpsModelExtensions.get().getModelPreferences().getNullPresentation();
        }
        DateFormat format = IIpsModelExtensions.get().getModelPreferences().getDateFormat();
        return format.format(validFrom.getTime());
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        IChangesOverTimeNamingConvention namingConvention = IIpsModelExtensions.get().getModelPreferences()
                .getChangesOverTimeNamingConvention();
        if (cachedImageDescriptor == null || cachedNamingConvention != namingConvention) {
            cachedNamingConvention = namingConvention;
            cachedImageDescriptor = getGenerationImageDescriptor(namingConvention);
        }
        return cachedImageDescriptor;
    }

    public ImageDescriptor getGenerationImageDescriptor(IChangesOverTimeNamingConvention namingConvention) {
        String id = namingConvention.getId();
        ImageDescriptor imageDescriptor = IIpsDecorators.getImageHandling().createImageDescriptor(
                id + "_" + GENERATION_IMAGE_BASE + ".gif"); //$NON-NLS-1$ //$NON-NLS-2$

        // if image does not exist try to load default image
        if (!exists(imageDescriptor)) {
            imageDescriptor = IIpsDecorators.getImageHandling().createImageDescriptor(GENERATION_IMAGE_BASE + ".gif"); //$NON-NLS-1$
        }

        return imageDescriptor;
    }

    private boolean exists(ImageDescriptor imageDescriptor) {
        return (imageDescriptor != null && imageDescriptor != ImageDescriptor.getMissingImageDescriptor());
    }
}
