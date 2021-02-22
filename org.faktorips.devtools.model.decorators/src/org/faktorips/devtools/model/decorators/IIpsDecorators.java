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

public interface IIpsDecorators {

    static IIpsDecorators get() {
        @SuppressWarnings("deprecation")
        IpsDecorators ipsDecorators = IpsDecorators.get();
        return ipsDecorators;
    }

    static IIpsElementDecorator get(Class<? extends IIpsElement> ipsElementClass) {
        return get().getDecorator(ipsElementClass);
    }

    static ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        if (ipsElement != null) {
            IIpsElementDecorator iIpsElementDecorator = get(ipsElement.getClass());
            return iIpsElementDecorator.getImageDescriptor(ipsElement);
        } else {
            return ImageDescriptor.getMissingImageDescriptor();
        }
    }

    static ImageDescriptor getDefaultImageDescriptor(Class<? extends IIpsElement> ipsElementClass) {
        return ipsElementClass != null
                ? get(ipsElementClass).getDefaultImageDescriptor()
                : ImageDescriptor.getMissingImageDescriptor();
    }

    static IImageHandling getImageHandling() {
        return IpsDecorators.getImageHandling();
    }

    IIpsElementDecorator getDecorator(Class<? extends IIpsElement> ipsElementClass);

    public Collection<Class<? extends IIpsElement>> getDecoratedClasses();

}
