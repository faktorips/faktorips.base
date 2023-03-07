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
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsElementDecorator;
import org.faktorips.devtools.model.ipsproject.ILibraryIpsPackageFragmentRoot;

public class LibraryIpsPackageFragmentRootDecorator implements IIpsElementDecorator {

    public static final String IPS_ARCHIVE_IMAGE = "IpsAr.gif"; //$NON-NLS-1$
    public static final String IPS_FOLDER_IMAGE = "IpsFolder.gif"; //$NON-NLS-1$

    @Override
    public ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        if (ipsElement instanceof ILibraryIpsPackageFragmentRoot packageFragmentRoot) {
            if (packageFragmentRoot.isContainedInArchive()) {
                return IIpsDecorators.getImageHandling().getSharedImageDescriptor(IPS_ARCHIVE_IMAGE, true);
            }
            return IIpsDecorators.getImageHandling().getSharedImageDescriptor(IPS_FOLDER_IMAGE, true);
        } else {
            return getDefaultImageDescriptor();
        }
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IIpsDecorators.getImageHandling().getSharedImageDescriptor(IPS_ARCHIVE_IMAGE, true);
    }
}
