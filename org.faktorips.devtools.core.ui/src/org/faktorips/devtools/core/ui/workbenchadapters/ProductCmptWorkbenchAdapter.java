/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin.ImageHandling;

/**
 * TODO Support Disabled Icons
 * 
 * @author Cornelius Dirmeier, Stefan Widmaier, FaktorZehn AG
 */
public class ProductCmptWorkbenchAdapter extends IpsObjectWorkbenchAdapter {

    private static final String PRODUCT_TEMPLATE_OVERLAY = "ProductTemplateOverlay.gif"; //$NON-NLS-1$

    // using imageRegistry and not the resource manager because images have to be read by input
    // stream to support all kinds of images in user projects, including images in archives.
    // The image registry uses the UIPlugin's resource manager internally
    private ImageRegistry imageRegistry;

    private ImageDescriptor prodCmptDefaultIcon;

    private ImageDescriptor productTemplateIcon;

    public ProductCmptWorkbenchAdapter() {
        super();
        prodCmptDefaultIcon = IpsUIPlugin.getImageHandling().createImageDescriptor("ProductCmpt.gif"); //$NON-NLS-1$
        productTemplateIcon = IpsUIPlugin.getImageHandling().createImageDescriptor("ProductTemplate.gif"); //$NON-NLS-1$
    }

    private ImageRegistry getImageRegistry() {
        if (imageRegistry == null) {
            imageRegistry = new ImageRegistry(IpsUIPlugin.getImageHandling().getResourceManager());
        }
        return imageRegistry;

    }

    private ImageDescriptor getProductCmptImageDescriptor(IProductCmptType type) {
        IconDesc icon = getProductCmptIconDesc(type);
        return icon.getImageDescriptor();
    }

    /**
     * Package private for testing purposes.
     */
    /* private */IconDesc getProductCmptIconDesc(IProductCmptType type) {
        if (type == null) {
            return new DefaultIconDesc();
        }

        if (type.isUseCustomInstanceIcon()) {
            return new PathIconDesc(type.getIpsProject(), type.getInstancesIcon());
        } else if (type.hasSupertype()) {
            IProductCmptType superType;
            superType = (IProductCmptType)type.findSupertype(type.getIpsProject());
            return getProductCmptIconDesc(superType);
        } else {
            return new DefaultIconDesc();
        }
    }

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsSrcFile ipsSrcFile) {
        boolean template = ipsSrcFile.getIpsObjectType().equals(IpsObjectType.PRODUCT_TEMPLATE);
        if (template) {
            return productTemplateIcon;
        } else {
            String typeName = ipsSrcFile.getPropertyValue(IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE);
            if (typeName != null) {
                IProductCmptType type = ipsSrcFile.getIpsProject().findProductCmptType(typeName);
                ImageDescriptor productCmptImageDescriptor = getProductCmptImageDescriptor(type);
                String templateName = ipsSrcFile.getPropertyValue(IProductCmpt.PROPERTY_TEMPLATE);
                if (StringUtils.isNotBlank(templateName)) {
                    ImageHandling imageHandling = IpsUIPlugin.getImageHandling();
                    return imageHandling.getSharedOverlayImageDescriptor(
                            imageHandling.getImage(productCmptImageDescriptor), PRODUCT_TEMPLATE_OVERLAY,
                            IDecoration.TOP_LEFT);
                }
                return productCmptImageDescriptor;
            }
        }
        return getDefaultImageDescriptor();
    }

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObject ipsObject) {
        if (ipsObject instanceof IProductCmpt) {
            IProductCmpt productCmpt = (IProductCmpt)ipsObject;
            if (productCmpt.isProductTemplate()) {
                return productTemplateIcon;
            } else {
                return getProductCmptImageDescriptor(productCmpt.findProductCmptType(ipsObject.getIpsProject()));
            }
        }
        return null;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return prodCmptDefaultIcon;
    }

    public ImageDescriptor getImageDescriptorForInstancesOf(IProductCmptType type) {
        return getProductCmptImageDescriptor(type);
    }

    public abstract class IconDesc {
        public abstract ImageDescriptor getImageDescriptor();
    }

    public class PathIconDesc extends IconDesc {
        private IIpsProject ipsProject;
        private String pathToImage;

        public PathIconDesc(IIpsProject ipsProject, String pathToImage) {
            this.ipsProject = ipsProject;
            this.pathToImage = pathToImage;
        }

        @Override
        public ImageDescriptor getImageDescriptor() {
            ImageDescriptor cachedImage = getImageRegistry().getDescriptor(pathToImage);
            if (cachedImage == null) {
                try {
                    InputStream inputStream = ipsProject.getResourceAsStream(pathToImage);
                    if (inputStream != null) {
                        Image loadedImage = new Image(Display.getDefault(), inputStream);
                        getImageRegistry().put(pathToImage, loadedImage);
                        ImageDescriptor imageDesc = getImageRegistry().getDescriptor(pathToImage);
                        inputStream.close();
                        return imageDesc;
                    } else {
                        return ImageDescriptor.getMissingImageDescriptor();
                    }
                } catch (IOException e) {
                    IpsPlugin.log(e);
                }
            }
            return cachedImage;
        }

        public String getPathToImage() {
            return pathToImage;
        }
    }

    public class DefaultIconDesc extends IconDesc {

        @Override
        public ImageDescriptor getImageDescriptor() {
            return prodCmptDefaultIcon;
        }

    }
}
