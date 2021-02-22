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
import org.faktorips.devtools.model.internal.testcasetype.TestAttribute;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;

public class TestAttributeDecorator implements IIpsObjectPartDecorator {

    @Override
    public ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof TestAttribute) {
            TestAttribute testAttribute = (TestAttribute)ipsObjectPart;
            try {
                // TODO v2 - hier koennen wir doch auch ein festes Image nehmen, oder? (von jörg)
                IPolicyCmptTypeAttribute attribute = testAttribute.findAttribute(testAttribute.getIpsProject());
                if (attribute != null) {
                    return IIpsDecorators.getImageHandling().getImageDescriptor(attribute);
                } else {
                    return getDefaultImageDescriptor();
                }
            } catch (CoreRuntimeException e) {
                // ignore exception, return default image
                return getDefaultImageDescriptor();
            }
        }
        return null;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IIpsDecorators.getImageHandling().getSharedImageDescriptor("TestAttribute.gif", true); //$NON-NLS-1$
    }

}
