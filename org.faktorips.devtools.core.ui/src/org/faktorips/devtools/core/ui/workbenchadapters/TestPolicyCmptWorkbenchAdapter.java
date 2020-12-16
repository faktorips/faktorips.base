/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;

public class TestPolicyCmptWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof ITestPolicyCmpt) {
            ITestPolicyCmpt testPC = (ITestPolicyCmpt)ipsObjectPart;
            String baseImageName = "PolicyCmptInstance.gif"; //$NON-NLS-1$
            if (testPC.isProductRelevant()) {
                return IpsUIPlugin.getImageHandling().getSharedOverlayImage(baseImageName, OverlayIcons.PRODUCT_OVR,
                        IDecoration.TOP_RIGHT);
            } else {
                return IpsUIPlugin.getImageHandling().getSharedImageDescriptor(baseImageName, true);
            }
        }
        return null;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("PolicyCmptInstance.gif", true); //$NON-NLS-1$
    }

}
