/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.decorators.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.decorators.IImageHandling;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsSrcFileDecorator;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;

public class ProductCmptDecorator implements IIpsSrcFileDecorator {

    public static final String PRODUCT_CMPT_BASE_IMAGE = "ProductCmpt.gif"; //$NON-NLS-1$
    public static final String PRODUCT_CMPT_TEMPLATE_BASE_IMAGE = "ProductTemplate.gif"; //$NON-NLS-1$

    private final String defaultImage;

    private final Map<String, IconDesc> iconsByPath = new HashMap<>();

    ProductCmptDecorator() {
        this(PRODUCT_CMPT_BASE_IMAGE);
    }

    private ProductCmptDecorator(String defaultImage) {
        this.defaultImage = defaultImage;
    }

    static ProductCmptDecorator forTemplates() {
        return new ProductCmptDecorator(PRODUCT_CMPT_TEMPLATE_BASE_IMAGE);
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IIpsDecorators.getImageHandling().createImageDescriptor(defaultImage);
    }

    @Override
    public ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        if (ipsElement instanceof IProductCmpt) {
            IProductCmpt productCmpt = (IProductCmpt)ipsElement;
            if (productCmpt.isProductTemplate()) {
                return IIpsDecorators.getImageHandling().createImageDescriptor(PRODUCT_CMPT_TEMPLATE_BASE_IMAGE);
            } else {
                return getProductCmptImageDescriptor(productCmpt.findProductCmptType(ipsElement.getIpsProject()));
            }
        }
        if (ipsElement instanceof IpsSrcFile) {
            return getImageDescriptor((IIpsSrcFile)ipsElement);
        }
        return IIpsSrcFileDecorator.super.getImageDescriptor(ipsElement);
    }

    @Override
    public ImageDescriptor getImageDescriptor(IIpsSrcFile ipsSrcFile) {
        if (ipsSrcFile != null) {
            boolean template = ipsSrcFile.getIpsObjectType().equals(IpsObjectType.PRODUCT_TEMPLATE);
            if (template) {
                return IIpsDecorators.getImageHandling().createImageDescriptor(PRODUCT_CMPT_TEMPLATE_BASE_IMAGE);
            } else {
                String typeName = ipsSrcFile.getPropertyValue(IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE);
                if (typeName != null) {
                    IProductCmptType type = ipsSrcFile.getIpsProject().findProductCmptType(typeName);
                    ImageDescriptor productCmptImageDescriptor = getProductCmptImageDescriptor(type);
                    String templateName = ipsSrcFile.getPropertyValue(IProductCmpt.PROPERTY_TEMPLATE);
                    if (templateName != null && !templateName.isBlank()) {
                        IImageHandling imageHandling = IIpsDecorators.getImageHandling();
                        return imageHandling.getSharedOverlayImageDescriptor(
                                imageHandling.getImage(productCmptImageDescriptor),
                                OverlayIcons.TEMPLATE,
                                IDecoration.TOP_LEFT);
                    }
                    return productCmptImageDescriptor;
                }
            }
        }
        return getDefaultImageDescriptor();
    }

    public ImageDescriptor getImageDescriptorForInstancesOf(IProductCmptType type) {
        return getProductCmptImageDescriptor(type);
    }

    private ImageDescriptor getProductCmptImageDescriptor(IProductCmptType type) {
        IconDesc icon = getProductCmptIconDesc(type);
        return icon.getImageDescriptor();
    }

    IconDesc getProductCmptIconDesc(IProductCmptType type) {
        if (type != null && type.isUseCustomInstanceIcon()) {
            return iconsByPath.computeIfAbsent(type.getInstancesIcon(), i -> new PathIconDesc(type.getIpsProject(), i));
        } else if (type != null && type.hasSupertype()) {
            IProductCmptType superType;
            superType = (IProductCmptType)type.findSupertype(type.getIpsProject());
            return getProductCmptIconDesc(superType);
        }
        return this::getDefaultImageDescriptor;
    }
}
