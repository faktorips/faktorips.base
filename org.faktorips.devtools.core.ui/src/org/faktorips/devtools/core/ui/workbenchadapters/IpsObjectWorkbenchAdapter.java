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

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.util.StringUtil;

public abstract class IpsObjectWorkbenchAdapter extends IpsElementWorkbenchAdapter {

    public IpsObjectWorkbenchAdapter() {
        super();
    }

    @Override
    protected final ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsSrcFile) {
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)ipsElement;
            return getImageDescriptor(ipsSrcFile);
        } else if (ipsElement instanceof IIpsObject) {
            IIpsObject ipsObject = (IIpsObject)ipsElement;
            return getImageDescriptor(ipsObject);
        }
        return null;
    }

    protected abstract ImageDescriptor getImageDescriptor(IIpsSrcFile ipsSrcFile);

    protected abstract ImageDescriptor getImageDescriptor(IIpsObject ipsObject);

    @Override
    protected final String getLabel(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsSrcFile) {
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)ipsElement;
            return getLabel(ipsSrcFile);
        } else if (ipsElement instanceof IIpsObject) {
            IIpsObject ipsObject = (IIpsObject)ipsElement;
            return getLabel(ipsObject);
        }
        return ipsElement.getName();
    }

    protected String getLabel(IIpsSrcFile ipsSrcFile) {
        return StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName());
    }

    protected String getLabel(IIpsObject ipsObject) {
        return ipsObject.getName();
    }

}
