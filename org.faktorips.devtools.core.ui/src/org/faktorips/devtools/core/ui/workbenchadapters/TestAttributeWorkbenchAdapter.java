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

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.testcasetype.TestAttribute;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class TestAttributeWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof TestAttribute) {
            TestAttribute testAttribute = (TestAttribute)ipsObjectPart;
            try {
                // TODO v2 - hier koennen wir doch auch ein festes Image nehmen, oder? (von jörg)
                IPolicyCmptTypeAttribute attribute = testAttribute.findAttribute(testAttribute.getIpsProject());
                if (attribute != null) {
                    return IpsUIPlugin.getImageHandling().getImageDescriptor(attribute);
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
        return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("DeltaTypeMissingPropertyValue.gif", true); //$NON-NLS-1$
    }

}
