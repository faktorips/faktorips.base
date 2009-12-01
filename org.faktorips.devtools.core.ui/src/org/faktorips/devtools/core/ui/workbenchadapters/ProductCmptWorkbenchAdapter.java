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
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class ProductCmptWorkbenchAdapter extends IpsObjectWorkbenchAdapter {

    public ProductCmptWorkbenchAdapter() {
        super(null);
    }

    public String getEnclosingLabel(IIpsSrcFile ipsSrcFile) throws CoreException {
        return ipsSrcFile.getPropertyValue(IIpsElement.PROPERTY_NAME);
    }

    private ImageDescriptor getProductCmptImage(IProductCmptType type) {
        ImageDescriptor result = null;
        // TODO get custom image for this prodCmpt from ProdCmptType
        if (result == null) {
            return IpsUIPlugin.getDefault().getImageDescriptor("ProductCmpt.gif");
        }
        return result;
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

    @Override
    protected String getLabel(IIpsSrcFile ipsSrcFile) {
        try {
            return ipsSrcFile.getPropertyValue(IIpsElement.PROPERTY_NAME);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return "";
        }
    }

    @Override
    protected String getLabel(IIpsObject ipsObject) {
        return ipsObject.getName();
    }
}
