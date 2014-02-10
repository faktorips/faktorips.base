/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.Messages;

public class IpsPackageFragmentWorkbenchAdapter extends IpsElementWorkbenchAdapter {

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsPackageFragment) {
            IIpsPackageFragment packageFragment = (IIpsPackageFragment)ipsElement;
            try {
                IIpsElement[] children = packageFragment.getChildren();
                if (children != null && children.length > 0) {
                    return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("IpsPackageFragment.gif", true); //$NON-NLS-1$
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
            return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("IpsPackageFragmentEmpty.gif", true); //$NON-NLS-1$
        } else {
            return null;
        }
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("IpsPackageFragment.gif", true); //$NON-NLS-1$
    }

    @Override
    protected String getLabel(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsPackageFragment) {
            IIpsPackageFragment packageFragment = (IIpsPackageFragment)ipsElement;
            if (StringUtils.isEmpty(packageFragment.getName())) {
                return Messages.DefaultLabelProvider_labelDefaultPackage;
            }
        }
        return super.getLabel(ipsElement);
    }

}
