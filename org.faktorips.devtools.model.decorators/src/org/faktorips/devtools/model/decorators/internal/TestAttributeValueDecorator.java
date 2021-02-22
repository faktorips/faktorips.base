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
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsObjectPartDecorator;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;

public class TestAttributeValueDecorator implements IIpsObjectPartDecorator {

    @Override
    public ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof ITestAttributeValue) {
            ITestAttributeValue attrValue = (ITestAttributeValue)ipsObjectPart;
            try {
                // TODO v2 - Verwendung fixes image
                ITestAttribute testAttribute = attrValue.findTestAttribute(attrValue.getIpsProject());
                if (testAttribute != null) {
                    return IIpsDecorators.getImageHandling().getImageDescriptor(testAttribute);
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
        return IIpsDecorators.getImageHandling().getSharedImageDescriptor("TestAttributeValue.gif", true); //$NON-NLS-1$
    }

}
