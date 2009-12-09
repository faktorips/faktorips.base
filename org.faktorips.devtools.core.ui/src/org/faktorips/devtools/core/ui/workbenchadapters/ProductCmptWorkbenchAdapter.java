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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * TODO Support Disabled Icons
 * 
 * @author Cornelius Dirmeier, Stefan Widmaier, FaktorZehn AG
 */
public class ProductCmptWorkbenchAdapter extends IpsObjectWorkbenchAdapter {

    private ImageDescriptor prodCmptDefaultIcon;

    public ProductCmptWorkbenchAdapter() {
        super();
        prodCmptDefaultIcon = IpsUIPlugin.getDefault().getImageDescriptor("ProductCmpt.gif");
    }

    private ImageDescriptor getProductCmptImage(IProductCmptType type) {
        if (type == null) {
            return getDefaultImageDescriptor();
        }

        if (type.isUseCustomInstanceIcon()) {
            return getImageDescriptorForPath(type.getIpsProject(), type.getInstancesIcon());
        } else if (type.hasSupertype()) {
            IProductCmptType superType;
            try {
                superType = (IProductCmptType)type.findSupertype(type.getIpsProject());
            } catch (CoreException e) {
                return getDefaultImageDescriptor();
            }
            return getProductCmptImage(superType);
        } else {
            return getDefaultImageDescriptor();
        }
    }

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsSrcFile ipsSrcFile) {
        try {
            String typeName = ipsSrcFile.getPropertyValue(IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE);
            IProductCmptType type = ipsSrcFile.getIpsProject().findProductCmptType(typeName);
            return getProductCmptImage(type);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return null;
        }
    }

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObject ipsObject) {
        try {
            if (ipsObject instanceof IProductCmpt) {
                IProductCmpt productCmpt = (IProductCmpt)ipsObject;
                return getProductCmptImage(productCmpt.findProductCmptType(ipsObject.getIpsProject()));
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    protected ImageDescriptor getDefaultImageDescriptor() {
        return prodCmptDefaultIcon;
    }

    public ImageDescriptor getImageDescriptorForInstancesOf(IProductCmptType type) {
        return getProductCmptImage(type);
    }

}
