/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;

/**
 * Returns image and label of the contained IpsObject.
 * 
 * @author widmaier
 */
public class IpsSrcFileWorkbenchAdapter extends IpsElementWorkbenchAdapter {

    public IpsSrcFileWorkbenchAdapter() {
        super();
    }

    // private IWorkbenchAdapter getAdapter(IIpsElement ipsElement) {
    // if (ipsElement instanceof IIpsSrcFile) {
    // IIpsSrcFile ipsSrcFile = (IIpsSrcFile)ipsElement;
    // return (IWorkbenchAdapter)ipsSrcFile.getIpsObjectType().newObject(ipsSrcFile).getAdapter(
    // IWorkbenchAdapter.class);
    // }
    // return null;
    // }

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        // IWorkbenchAdapter adapter = getAdapter(ipsElement);
        // if (adapter != null) {
        // return adapter.getImageDescriptor(ipsElement);
        // }
        // return null;
        return IpsPlugin.getDefault().getImageDescriptor("IpsSrcFile.gif"); //$NON-NLS-1$
    }

    // @Override
    // protected String getLabel(IIpsElement ipsElement) {
    // IWorkbenchAdapter adapter = getAdapter(ipsElement);
    // if (adapter != null) {
    // return adapter.getLabel(ipsElement);
    // }
    // return "";
    // }

}
