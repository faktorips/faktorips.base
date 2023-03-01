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
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsObjectPartDecorator;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.runtime.internal.IpsStringUtils;

public class TestPolicyCmptTypeParameterDecorator implements IIpsObjectPartDecorator {

    // TODO Überlegen, ob hier passendere statische Bilder gefunden werden können
    @Override
    public ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        /*
         * Old comment: If no association is specified then return the policy cmpt type image or if
         * a product cmpt is required the the product cmpt image. If a association is specified then
         * return the image which is provided by the association or if the association is not found
         * the default "association.gif" image.
         */
        if (ipsObjectPart instanceof ITestPolicyCmptTypeParameter testParameter) {
            if (IpsStringUtils.isEmpty(testParameter.getAssociation())) {
                if (testParameter.isRequiresProductCmpt()) {
                    return IIpsDecorators.getImageHandling()
                            .getSharedImageDescriptor(IpsDecorators.PRODUCT_CMPT_TYPE_IMAGE, true);
                } else {
                    return IIpsDecorators.getImageHandling()
                            .getSharedImageDescriptor(IpsDecorators.POLICY_CMPT_TYPE_IMAGE, true);
                }
            }
            if (!testParameter.isRoot()) {
                try {
                    IPolicyCmptTypeAssociation association = testParameter.findAssociation(testParameter
                            .getIpsProject());
                    if (association != null) {
                        return IIpsDecorators.getImageDescriptor(association);
                    }
                } catch (IpsException e) {
                    IpsLog.log(e);
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
