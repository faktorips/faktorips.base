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
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.util.StringUtil;

/**
 * An {@link IIpsElementDecorator} for {@link IIpsSrcFile IIpsSrcFiles}.
 *
 * @since 21.6
 */
public interface IIpsSrcFileDecorator extends IIpsElementDecorator {

    @Override
    default ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsSrcFile) {
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)ipsElement;
            return getImageDescriptor(ipsSrcFile);
        }
        return getDefaultImageDescriptor();
    }

    /**
     * Returns the {@link ImageDescriptor} for the given {@link IIpsSrcFile}.
     */
    ImageDescriptor getImageDescriptor(IIpsSrcFile ipsSrcFile);

    @Override
    default String getLabel(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsSrcFile) {
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)ipsElement;
            return getLabel(ipsSrcFile);
        }
        return IIpsElementDecorator.super.getLabel(ipsElement);
    }

    /**
     * Returns the label for the given {@link IIpsSrcFile}.
     */
    default String getLabel(IIpsSrcFile ipsSrcFile) {
        return StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName());
    }
}
