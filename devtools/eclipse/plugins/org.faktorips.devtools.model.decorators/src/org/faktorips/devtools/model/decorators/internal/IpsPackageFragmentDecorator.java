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
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsElementDecorator;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.runtime.internal.IpsStringUtils;

public class IpsPackageFragmentDecorator implements IIpsElementDecorator {

    public static final String IPS_PACKAGE_FRAGMENT_ICON = "IpsPackageFragment.gif"; //$NON-NLS-1$
    public static final String IPS_PACKAGE_FRAGMENT_EMPTY_ICON = "IpsPackageFragmentEmpty.gif"; //$NON-NLS-1$

    @Override
    public ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsPackageFragment packageFragment) {
            try {
                IIpsElement[] children = packageFragment.getChildren();
                if (children != null && children.length > 0) {
                    return IIpsDecorators.getImageHandling().getSharedImageDescriptor(IPS_PACKAGE_FRAGMENT_ICON, true);
                }
            } catch (IpsException e) {
                IpsLog.log(e);
            }
            return IIpsDecorators.getImageHandling().getSharedImageDescriptor(IPS_PACKAGE_FRAGMENT_EMPTY_ICON, true);
        } else {
            return getDefaultImageDescriptor();
        }
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IIpsDecorators.getImageHandling().getSharedImageDescriptor(IPS_PACKAGE_FRAGMENT_ICON, true);
    }

    @Override
    public String getLabel(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsPackageFragment packageFragment) {
            if (IpsStringUtils.isEmpty(packageFragment.getName())) {
                return Messages.DefaultLabelProvider_labelDefaultPackage;
            }
        }
        return IIpsElementDecorator.super.getLabel(ipsElement);
    }

}
