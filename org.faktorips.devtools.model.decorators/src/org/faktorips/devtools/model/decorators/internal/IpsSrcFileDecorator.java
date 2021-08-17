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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsElementDecorator;
import org.faktorips.devtools.model.decorators.IIpsSrcFileDecorator;
import org.faktorips.devtools.model.internal.ipsobject.IpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.plugin.IpsLog;

public class IpsSrcFileDecorator implements IIpsSrcFileDecorator {

    public static final String IPS_SRC_FILE_IMAGE = "IpsSrcFile.gif"; //$NON-NLS-1$

    private final ImageDescriptor defaultImageDescriptor = IIpsDecorators.getImageHandling()
            .getSharedImageDescriptor(IPS_SRC_FILE_IMAGE, true);

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return defaultImageDescriptor;
    }

    @Override
    public ImageDescriptor getImageDescriptor(IIpsSrcFile ipsSrcFile) {
        try {
            if (ipsSrcFile != null && ipsSrcFile.exists() && ipsSrcFile.isContentParsable()) {
                Class<? extends IpsObject> implementingClass = ipsSrcFile.getIpsObjectType().getImplementingClass();
                if (implementingClass != null) {
                    IIpsElementDecorator decorator = IIpsDecorators.get(implementingClass);
                    if (!decorator.equals(IIpsElementDecorator.MISSING_ICON_PROVIDER)) {
                        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(ipsSrcFile);
                        if (!imageDescriptor.equals(ImageDescriptor.getMissingImageDescriptor())) {
                            return imageDescriptor;
                        }
                    }
                }
            }
        } catch (CoreException e) {
            IpsLog.log(e);
        }
        return defaultImageDescriptor;
    }

}
