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
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * Returns image and label of the contained IpsObject.
 * 
 * @author widmaier
 */
public class IpsSrcFileWorkbenchAdapter extends IpsElementWorkbenchAdapter {

    public IpsSrcFileWorkbenchAdapter() {
    }

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        Class ipsObjectClass = ProductCmpt.class;
        IpsObjectWorkbenchAdapter adapter = (IpsObjectWorkbenchAdapter)IpsUIPlugin.getDefault().getAdapterFactory()
                .getAdapter(ipsObjectClass, IWorkbenchAdapter.class);
        return adapter.getImageDescriptor((IIpsSrcFile)ipsElement);
    }

    @Override
    protected String getLabel(IIpsElement ipsElement) {
        return ((IIpsSrcFile)ipsElement).getIpsObjectName();
    }

}
