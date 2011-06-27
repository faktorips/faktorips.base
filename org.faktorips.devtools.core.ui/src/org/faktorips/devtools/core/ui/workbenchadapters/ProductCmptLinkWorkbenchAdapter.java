/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class ProductCmptLinkWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IProductCmptLink) {
            IProductCmptLink link = (IProductCmptLink)ipsObjectPart;
            try {
                IProductCmpt findTarget = link.findTarget(ipsObjectPart.getIpsProject());
                return IpsUIPlugin.getImageHandling().getImageDescriptor(findTarget);
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return getDefaultImageDescriptor();
            }
        }
        return null;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("ProductCmptLink.gif", true); //$NON-NLS-1$;
    }

    @Override
    protected String getLabel(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IProductCmptLink) {
            IProductCmptLink link = (IProductCmptLink)ipsObjectPart;
            try {
                IProductCmpt findTarget = link.findTarget(ipsObjectPart.getIpsProject());
                if (findTarget == null) {
                    return StringUtils.EMPTY;
                }
                return findTarget.getName();
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        return super.getLabel(ipsObjectPart);
    }

}
