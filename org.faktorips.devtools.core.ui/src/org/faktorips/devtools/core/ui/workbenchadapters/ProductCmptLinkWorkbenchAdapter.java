/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class ProductCmptLinkWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IProductCmptLink) {
            IProductCmptLink link = (IProductCmptLink)ipsObjectPart;
            try {
                IProductCmpt findTarget = link.findTarget(ipsObjectPart.getIpsProject());
                return IpsUIPlugin.getImageHandling().getImageDescriptor(findTarget);
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return getDefaultImageDescriptor();
            }
        }
        return null;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("ProductCmptLink.gif", true); //$NON-NLS-1$;
    }

    @Override
    protected String getLabel(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IProductCmptLink) {
            IProductCmptLink link = (IProductCmptLink)ipsObjectPart;
            try {
                IProductCmpt findTarget = link.findTarget(ipsObjectPart.getIpsProject());
                if (findTarget == null) {
                    return StringUtils.EMPTY;
                }
                return findTarget.getName();
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        return super.getLabel(ipsObjectPart);
    }

}
