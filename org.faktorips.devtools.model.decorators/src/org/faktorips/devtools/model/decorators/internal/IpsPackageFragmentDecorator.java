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
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsElementDecorator;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.runtime.internal.IpsStringUtils;

public class IpsPackageFragmentDecorator implements IIpsElementDecorator {

    @Override
    public ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsPackageFragment) {
            IIpsPackageFragment packageFragment = (IIpsPackageFragment)ipsElement;
            try {
                IIpsElement[] children = packageFragment.getChildren();
                if (children != null && children.length > 0) {
                    return IIpsDecorators.getImageHandling().getSharedImageDescriptor("IpsPackageFragment.gif", true); //$NON-NLS-1$
                }
            } catch (CoreException e) {
                IpsLog.log(e);
            }
            return IIpsDecorators.getImageHandling().getSharedImageDescriptor("IpsPackageFragmentEmpty.gif", true); //$NON-NLS-1$
        } else {
            return null;
        }
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IIpsDecorators.getImageHandling().getSharedImageDescriptor("IpsPackageFragment.gif", true); //$NON-NLS-1$
    }

    @Override
    public String getLabel(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsPackageFragment) {
            IIpsPackageFragment packageFragment = (IIpsPackageFragment)ipsElement;
            if (IpsStringUtils.isEmpty(packageFragment.getName())) {
                return Messages.DefaultLabelProvider_labelDefaultPackage;
            }
        }
        return IIpsElementDecorator.super.getLabel(ipsElement);
    }

}
