/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.internal.model.ipsproject.LibraryIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class LibraryIpsPackageFragmentRootWorkbenchAdapter extends IpsElementWorkbenchAdapter {

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        if (ipsElement instanceof LibraryIpsPackageFragmentRoot) {
            LibraryIpsPackageFragmentRoot packageFragmentRoot = (LibraryIpsPackageFragmentRoot)ipsElement;
            if (packageFragmentRoot.isContainedInArchive()) {
                return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("IpsAr.gif", true); //$NON-NLS-1$
            }
            return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("IpsFolder.gif", true); //$NON-NLS-1$
        } else {
            return null;
        }
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("IpsAr.gif", true); //$NON-NLS-1$
    }
}
