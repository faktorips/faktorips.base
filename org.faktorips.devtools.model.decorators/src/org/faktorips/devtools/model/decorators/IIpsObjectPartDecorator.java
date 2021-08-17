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
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * An {@link IIpsElementDecorator} for {@link IIpsObjectPart IIpsObjectParts}.
 *
 * @since 21.6
 */
public interface IIpsObjectPartDecorator extends IIpsElementDecorator {

    @Override
    default ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsObjectPart) {
            IIpsObjectPart ipsObjectPart = (IIpsObjectPart)ipsElement;
            return getImageDescriptor(ipsObjectPart);
        }
        return getDefaultImageDescriptor();
    }

    /**
     * Returns the {@link ImageDescriptor} for the given {@link IIpsObjectPart}.
     */
    ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart);

    @Override
    default String getLabel(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsObjectPart) {
            IIpsObjectPart ipsObjectPart = (IIpsObjectPart)ipsElement;
            return getLabel(ipsObjectPart);
        }
        return IpsStringUtils.EMPTY;
    }

    /**
     * Returns the label for the given {@link IIpsObjectPart}.
     */
    default String getLabel(IIpsObjectPart ipsObjectPart) {
        return ipsObjectPart == null ? IpsStringUtils.EMPTY : ipsObjectPart.getName();
    }

}