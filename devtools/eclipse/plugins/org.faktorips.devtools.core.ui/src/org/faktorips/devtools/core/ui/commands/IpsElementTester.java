/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IResource;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * Tests some properties of ips elemtns for example whether the element is ediable or not. The
 * receiver object needs to be an ips element.
 * 
 * @author dirmeier
 */
public class IpsElementTester extends PropertyTester {

    /**
     * Check wether the receiver element is editable
     */
    public static final String PROPERTY_EDITABLE = "isEditable"; //$NON-NLS-1$

    /**
     * Check wether the receivers container is editable
     */
    public static final String PROPERTY_CONTAINER_EDITABLE = "isContainerEditable"; //$NON-NLS-1$

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (!(receiver instanceof IIpsElement ipsElement)) {
            return false;
        }
        if (PROPERTY_EDITABLE.equals(property)) {
            return isEditable(ipsElement);
        } else if (PROPERTY_CONTAINER_EDITABLE.equals(property)) {
            return isContainerEditable(ipsElement);
        } else {
            return false;
        }
    }

    private boolean isEditable(IIpsElement ipsElement) {
        if (IpsPlugin.getDefault().getIpsPreferences().isWorkingModeBrowse() || ipsElement.isContainedInArchive()) {
            return false;
        }
        if (ipsElement instanceof IIpsObjectPartContainer) {
            return isEditable(((IIpsObjectPartContainer)ipsElement).getIpsSrcFile());
        }
        if (ipsElement instanceof IIpsSrcFile ipsSrcFile) {
            return ipsSrcFile.isMutable();
        }
        IResource resource = ipsElement.getAdapter(IResource.class);
        if (resource != null && resource.getResourceAttributes() != null) {
            return !resource.getResourceAttributes().isReadOnly();
        }
        return true;
    }

    private boolean isContainerEditable(IIpsElement ipsElement) {
        if (ipsElement.getParent() == null) {
            return isEditable(ipsElement);
        }
        return isEditable(ipsElement.getParent());
    }

}
