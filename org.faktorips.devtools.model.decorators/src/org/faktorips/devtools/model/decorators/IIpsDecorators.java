/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.decorators;

import java.util.Collection;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.decorators.internal.IpsDecorators;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;

public interface IIpsDecorators {

    static IIpsDecorators get() {
        @SuppressWarnings("deprecation")
        IpsDecorators ipsDecorators = IpsDecorators.get();
        return ipsDecorators;
    }

    static IIpsElementDecorator get(Class<? extends IIpsElement> ipsElementClass) {
        return get().getDecorator(ipsElementClass);
    }

    static IIpsElementDecorator get(IpsObjectType ipsObjectType) {
        return get().getDecorator(ipsObjectType);
    }

    static ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        if (ipsElement != null) {
            IIpsElementDecorator ipsElementDecorator = get(ipsElement.getClass());
            return ipsElementDecorator.getImageDescriptor(ipsElement);
        } else {
            return ImageDescriptor.getMissingImageDescriptor();
        }
    }

    static ImageDescriptor getDefaultImageDescriptor(Class<? extends IIpsElement> ipsElementClass) {
        return ipsElementClass != null
                ? get(ipsElementClass).getDefaultImageDescriptor()
                : ImageDescriptor.getMissingImageDescriptor();
    }

    static ImageDescriptor getDefaultImageDescriptor(IpsObjectType ipsObjectType) {
        return ipsObjectType != null
                ? get(ipsObjectType).getDefaultImageDescriptor()
                : ImageDescriptor.getMissingImageDescriptor();
    }

    static IImageHandling getImageHandling() {
        return IpsDecorators.getImageHandling();
    }

    IIpsElementDecorator getDecorator(Class<? extends IIpsElement> ipsElementClass);

    IIpsElementDecorator getDecorator(IpsObjectType ipsObjectType);

    public Collection<Class<? extends IIpsElement>> getDecoratedClasses();

}
