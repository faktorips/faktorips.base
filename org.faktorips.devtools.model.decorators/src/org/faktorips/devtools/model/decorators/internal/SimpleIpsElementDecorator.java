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
import org.eclipse.jface.viewers.IDecoration;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsElementDecorator;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IVersionControlledElement;

public class SimpleIpsElementDecorator implements IIpsElementDecorator {

    private ImageDescriptor imageDescriptor;

    public SimpleIpsElementDecorator(ImageDescriptor imageDescriptor) {
        this.imageDescriptor = imageDescriptor != null ? imageDescriptor : ImageDescriptor.getMissingImageDescriptor();
    }

    SimpleIpsElementDecorator(String iconFileName) {
        this(IIpsDecorators.getImageHandling().getSharedImageDescriptor(iconFileName, true));
    }

    @Override
    public ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        if (ipsElement instanceof IVersionControlledElement
                && ((IVersionControlledElement)ipsElement).isDeprecated()) {
            return IIpsDecorators.getImageHandling().getSharedOverlayImageDescriptor(imageDescriptor,
                    OverlayIcons.DEPRECATED, IDecoration.BOTTOM_LEFT);
        } else if (ipsElement instanceof IIpsSrcFile) {
            @SuppressWarnings("deprecation")
            boolean cached = IpsModel.get().isCached((IIpsSrcFile)ipsElement);
            if (cached) {
                IIpsObject ipsObject = ((IIpsSrcFile)ipsElement).getIpsObject();
                if (ipsObject instanceof IVersionControlledElement
                        && ((IVersionControlledElement)ipsObject).isDeprecated()) {
                    return IIpsDecorators.getImageHandling().getSharedOverlayImageDescriptor(imageDescriptor,
                            OverlayIcons.DEPRECATED, IDecoration.BOTTOM_LEFT);
                }
            }
        }
        return imageDescriptor;
    }
}
