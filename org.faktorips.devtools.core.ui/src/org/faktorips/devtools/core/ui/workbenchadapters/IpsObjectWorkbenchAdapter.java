/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
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
