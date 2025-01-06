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

/**
 * Provides {@link IIpsElementDecorator IIpsElementDecorators} for Faktor-IPS elements.
 *
 * @since 21.6
 */
public interface IIpsDecorators {

    /**
     * Returns the singleton instance.
     */
    @SuppressWarnings("deprecation")
    static IIpsDecorators get() {
        return IpsDecorators.get();
    }

    /**
     * Returns the {@link IIpsElementDecorator} for the given element class.
     */
    static IIpsElementDecorator get(Class<? extends IIpsElement> ipsElementClass) {
        return get().getDecorator(ipsElementClass);
    }

    /**
     * Returns the {@link IIpsElementDecorator} for the given {@link IpsObjectType}.
     */
    static IIpsElementDecorator get(IpsObjectType ipsObjectType) {
        return get().getDecorator(ipsObjectType);
    }

    /**
     * Returns the {@link ImageDescriptor} for the given element.
     *
     * @see IIpsElementDecorator#getImageDescriptor(IIpsElement)
     */
    static ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        if (ipsElement != null) {
            IIpsElementDecorator ipsElementDecorator = get(ipsElement.getClass());
            return ipsElementDecorator.getImageDescriptor(ipsElement);
        } else {
            return ImageDescriptor.getMissingImageDescriptor();
        }
    }

    /**
     * Returns the default {@link ImageDescriptor} for the given element class.
     *
     * @see IIpsElementDecorator#getDefaultImageDescriptor()
     */
    static ImageDescriptor getDefaultImageDescriptor(Class<? extends IIpsElement> ipsElementClass) {
        return ipsElementClass != null
                ? get(ipsElementClass).getDefaultImageDescriptor()
                : ImageDescriptor.getMissingImageDescriptor();
    }

    /**
     * Returns the default {@link ImageDescriptor} for the given {@link IpsObjectType}.
     *
     * @see IIpsElementDecorator#getDefaultImageDescriptor()
     */
    static ImageDescriptor getDefaultImageDescriptor(IpsObjectType ipsObjectType) {
        return ipsObjectType != null
                ? get(ipsObjectType).getDefaultImageDescriptor()
                : ImageDescriptor.getMissingImageDescriptor();
    }

    /**
     * Returns the {@link IImageHandling} to be used for the decorators.
     */
    static IImageHandling getImageHandling() {
        return IpsDecorators.getImageHandling();
    }

    /**
     * Returns the {@link IIpsElementDecorator} for the given element class.
     */
    IIpsElementDecorator getDecorator(Class<? extends IIpsElement> ipsElementClass);

    /**
     * Returns the {@link IIpsElementDecorator} for the given {@link IpsObjectType}.
     */
    IIpsElementDecorator getDecorator(IpsObjectType ipsObjectType);

    /**
     * Returns all classes for which an {@link IIpsElementDecorator} can be returned.
     */
    Collection<Class<? extends IIpsElement>> getDecoratedClasses();

}
