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

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Provides decoration (images and labels) for {@link IIpsElement IIpsElements}.
 * <p>
 * Decorators only provide {@link ImageDescriptor} instances. Creating concrete images from those
 * descriptors is up to the caller.
 *
 * @since 21.6
 */
public interface IIpsElementDecorator {

    /**
     * An {@link IIpsElementDecorator} that creates the
     * {@link ImageDescriptor#getMissingImageDescriptor()} and uses the
     * {@link IIpsElement#getName()} as a label.
     */
    public static final IIpsElementDecorator MISSING_ICON_PROVIDER = $ -> ImageDescriptor.getMissingImageDescriptor();

    /**
     * Returns the {@link ImageDescriptor} for the given {@link IIpsElement}.
     */
    ImageDescriptor getImageDescriptor(IIpsElement ipsElement);

    /**
     * Returns the {@link ImageDescriptor} to be used for example when only the element's class is
     * known but no concrete instance is available.
     */
    default ImageDescriptor getDefaultImageDescriptor() {
        return getImageDescriptor(null);
    }

    /**
     * Returns the label for the given {@link IIpsElement}.
     *
     * @implSpec This should be a (localized) name for the element, optionally with some added
     *           identifying information like a datatype.
     */
    default String getLabel(IIpsElement ipsElement) {
        return ipsElement == null ? IpsStringUtils.EMPTY : ipsElement.getName();
    }

}
