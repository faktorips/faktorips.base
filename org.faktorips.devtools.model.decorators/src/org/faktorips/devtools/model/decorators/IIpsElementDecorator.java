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

public interface IIpsElementDecorator {

    public static final IIpsElementDecorator MISSING_ICON_PROVIDER = $ -> ImageDescriptor.getMissingImageDescriptor();

    ImageDescriptor getImageDescriptor(IIpsElement ipsElement);

    default ImageDescriptor getDefaultImageDescriptor() {
        return getImageDescriptor(null);
    }

    default String getLabel(IIpsElement ipsElement) {
        return ipsElement.getName();
    }

}
