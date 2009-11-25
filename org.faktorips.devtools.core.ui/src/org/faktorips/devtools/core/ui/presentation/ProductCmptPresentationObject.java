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

package org.faktorips.devtools.core.ui.presentation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class ProductCmptPresentationObject implements IPresentationObject {

    public Class<? extends IIpsElement> getIpsElementClass() {
        return IProductCmpt.class;
    }

    public Image getImage(IIpsElement ipsElement) throws CoreException {
        if (ipsElement instanceof IIpsSrcFile) {
            return getEnclosingImage((IIpsSrcFile)ipsElement);
        } else if (ipsElement instanceof IProductCmpt) {
            IProductCmpt productCmpt = (IProductCmpt)ipsElement;
            return getProductCmptImage(productCmpt.findProductCmptType(ipsElement.getIpsProject()));
        }
        return null;
    }

    public String getLabel(IIpsElement ipsElement) throws CoreException {
        if (ipsElement instanceof IIpsSrcFile) {
            return getEnclosingLabel((IIpsSrcFile)ipsElement);
        } else {
            return ipsElement.getName();
        }
    }

    public Image getEnclosingImage(IIpsSrcFile ipsSrcFile) throws CoreException {
        String typeName = ipsSrcFile.getPropertyValue(IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE);
        IProductCmptType type = ipsSrcFile.getIpsProject().findProductCmptType(typeName);
        return getProductCmptImage(type);
    }

    public String getEnclosingLabel(IIpsSrcFile ipsSrcFile) throws CoreException {
        return ipsSrcFile.getPropertyValue(IIpsElement.PROPERTY_NAME);
    }

    private Image getProductCmptImage(IProductCmptType type) {
        // TODO get custom image for this prodCmpt from ProdCmptType
        Image result = null;
        if (result == null) {
            return IpsUIPlugin.getDefault().getImage("ProductCmpt");
        }
        return result;
    }
}
