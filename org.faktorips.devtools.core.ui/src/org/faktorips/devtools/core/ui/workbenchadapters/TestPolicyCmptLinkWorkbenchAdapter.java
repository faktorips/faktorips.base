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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class TestPolicyCmptLinkWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {
    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof ITestPolicyCmptLink) {
            ITestPolicyCmptLink policyCmptLink = (ITestPolicyCmptLink)ipsObjectPart;
            if (policyCmptLink.isAccoziation()) {
                // return the linked product cmpt image if the target relates a product cmpt,
                // or return the linked policy cmpt if target not found or no product cmpt is
                // related
                try {
                    ITestPolicyCmpt cmpt = policyCmptLink.findTarget();
                    if (cmpt != null && cmpt.hasProductCmpt()) {
                        return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("LinkProductCmpt.gif", true); //$NON-NLS-1$
                    }
                } catch (CoreException e) {
                    // ignored exception, return default image
                }
                return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("LinkedPolicyCmptType.gif", true); //$NON-NLS-1$
            } else {
                try {
                    ITestPolicyCmptTypeParameter param = policyCmptLink.findTestPolicyCmptTypeParameter(policyCmptLink
                            .getIpsProject());
                    if (param != null) {
                        IPolicyCmptTypeAssociation association = param.findAssociation(policyCmptLink.getIpsProject());
                        if (association != null) {
                            return IpsUIPlugin.getImageHandling().getImageDescriptor(association);
                        }
                    }
                } catch (CoreException e) {
                    // ignore exception, return default image
                }
                return getDefaultImageDescriptor();
            }
        }
        return null;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("AssociationType-Composition.gif", true); //$NON-NLS-1$
    }
}
