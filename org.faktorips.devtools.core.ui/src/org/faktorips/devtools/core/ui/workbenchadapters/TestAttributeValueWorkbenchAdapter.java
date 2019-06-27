/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class TestAttributeValueWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof ITestAttributeValue) {
            ITestAttributeValue attrValue = (ITestAttributeValue)ipsObjectPart;
            try {
                // TODO v2 - verwendung fixes image
                ITestAttribute testAttribute = attrValue.findTestAttribute(attrValue.getIpsProject());
                if (testAttribute != null) {
                    return IpsUIPlugin.getImageHandling().getImageDescriptor(testAttribute);
                }
            } catch (CoreException e) {
                // ignore exception, return default image
            }
            return getDefaultImageDescriptor();
        }
        return null;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("MissingAttribute.gif", true); //$NON-NLS-1$
    }

}
