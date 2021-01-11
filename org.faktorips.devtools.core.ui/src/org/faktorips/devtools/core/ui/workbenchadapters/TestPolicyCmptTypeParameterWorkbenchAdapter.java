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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class TestPolicyCmptTypeParameterWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {

    // TODO Überlegen, ob hier passendere statische Bilder gefeunden werden können
    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        /*
         * Old comment: If no association is specified then return the policy cmpt type image or if
         * a product cmpt is required the the product cmpt image. If a association is specified then
         * return the image which is provided by the association or if the association is not found
         * the default "association.gif" image.
         */
        if (ipsObjectPart instanceof ITestPolicyCmptTypeParameter) {
            ITestPolicyCmptTypeParameter testParameter = (ITestPolicyCmptTypeParameter)ipsObjectPart;
            if (StringUtils.isEmpty(testParameter.getAssociation())) {
                if (testParameter.isRequiresProductCmpt()) {
                    return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("ProductCmpt.gif", true); //$NON-NLS-1$
                } else {
                    return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("PolicyCmptType.gif", true); //$NON-NLS-1$
                }
            }
            if (!testParameter.isRoot()) {
                try {
                    IPolicyCmptTypeAssociation association = testParameter.findAssociation(testParameter
                            .getIpsProject());
                    if (association != null) {
                        return IpsUIPlugin.getImageHandling().getImageDescriptor(association);
                    }
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }
            return getDefaultImageDescriptor();
        }
        return null;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("AssociationType-Composition.gif", true); //$NON-NLS-1$
    }

}
