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
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsObjectPartDecorator;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;

public class TestPolicyCmptDecorator implements IIpsObjectPartDecorator {

    public static final String POLICY_CMPT_INSTANCE_IMAGE = "PolicyCmptInstance.gif"; //$NON-NLS-1$

    @Override
    public ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof ITestPolicyCmpt) {
            ITestPolicyCmpt testPC = (ITestPolicyCmpt)ipsObjectPart;
            if (testPC.isProductRelevant()) {
                return IIpsDecorators.getImageHandling().getSharedOverlayImageDescriptor(POLICY_CMPT_INSTANCE_IMAGE,
                        OverlayIcons.PRODUCT_RELEVANT,
                        IDecoration.TOP_RIGHT);
            } else {
                return IIpsDecorators.getImageHandling().getSharedImageDescriptor(POLICY_CMPT_INSTANCE_IMAGE, true);
            }
        }
        return getDefaultImageDescriptor();
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IIpsDecorators.getImageHandling().getSharedImageDescriptor(POLICY_CMPT_INSTANCE_IMAGE, true);
    }

}
