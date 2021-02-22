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
import org.faktorips.devtools.model.internal.ipsproject.LibraryIpsPackageFragmentRoot;

public class LibraryIpsPackageFragmentRootDecorator implements IIpsElementDecorator {

    @Override
    public ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        if (ipsElement instanceof LibraryIpsPackageFragmentRoot) {
            LibraryIpsPackageFragmentRoot packageFragmentRoot = (LibraryIpsPackageFragmentRoot)ipsElement;
            if (packageFragmentRoot.isContainedInArchive()) {
                return IIpsDecorators.getImageHandling().getSharedImageDescriptor("IpsAr.gif", true); //$NON-NLS-1$
            }
            return IIpsDecorators.getImageHandling().getSharedImageDescriptor("IpsFolder.gif", true); //$NON-NLS-1$
        } else {
            return null;
        }
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IIpsDecorators.getImageHandling().getSharedImageDescriptor("IpsAr.gif", true); //$NON-NLS-1$
    }
}
