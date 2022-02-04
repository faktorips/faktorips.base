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
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsObjectPartDecorator;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;

public class TestPolicyCmptLinkDecorator implements IIpsObjectPartDecorator {
    public static final String LINKED_POLICY_CMPT_TYPE_IMAGE = "LinkedPolicyCmptType.gif"; //$NON-NLS-1$
    public static final String LINK_PRODUCT_CMPT_IMAGE = "LinkProductCmpt.gif"; //$NON-NLS-1$

    @Override
    public ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof ITestPolicyCmptLink) {
            ITestPolicyCmptLink policyCmptLink = (ITestPolicyCmptLink)ipsObjectPart;
            if (policyCmptLink.isAssociation()) {
                // return the linked product cmpt image if the target relates a product cmpt,
                // or return the linked policy cmpt if target not found or no product cmpt is
                // related
                ITestPolicyCmpt cmpt = policyCmptLink.findTarget();
                if (cmpt != null && cmpt.hasProductCmpt()) {
                    return IIpsDecorators.getImageHandling().getSharedImageDescriptor(LINK_PRODUCT_CMPT_IMAGE, true);
                }
                return IIpsDecorators.getImageHandling().getSharedImageDescriptor(LINKED_POLICY_CMPT_TYPE_IMAGE, true);
            } else {
                try {
                    ITestPolicyCmptTypeParameter param = policyCmptLink
                            .findTestPolicyCmptTypeParameter(policyCmptLink.getIpsProject());
                    if (param != null) {
                        IPolicyCmptTypeAssociation association = param.findAssociation(policyCmptLink.getIpsProject());
                        if (association != null) {
                            return IIpsDecorators.getImageDescriptor(association);
                        }
                    }
                } catch (CoreRuntimeException e) {
                    IpsLog.log(e);
                    // ignore exception, return default image
                }
            }
        }
        return getDefaultImageDescriptor();
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IIpsDecorators.getImageHandling()
                .getSharedImageDescriptor(AssociationDecorator.ASSOCIATION_TYPE_COMPOSITION_IMAGE, true);
    }
}
