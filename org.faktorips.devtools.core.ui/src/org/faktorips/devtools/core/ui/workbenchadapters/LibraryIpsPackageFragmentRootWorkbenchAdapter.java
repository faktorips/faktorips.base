/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.internal.model.ipsproject.LibraryIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class LibraryIpsPackageFragmentRootWorkbenchAdapter extends IpsElementWorkbenchAdapter {

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        if (ipsElement instanceof LibraryIpsPackageFragmentRoot) {
            LibraryIpsPackageFragmentRoot packageFragmentRoot = (LibraryIpsPackageFragmentRoot)ipsElement;
            if (packageFragmentRoot.isContainedInArchive()) {
                return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("IpsAr.gif", true); //$NON-NLS-1$
            }
            return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("IpsFolder.gif", true); //$NON-NLS-1$
        } else {
            return null;
        }
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("IpsAr.gif", true); //$NON-NLS-1$
    }
}
