/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * TODO Support Disabled Icons
 * 
 * @author Cornelius Dirmeier, Stefan Widmaier, FaktorZehn AG
 */
public class ProductCmptWorkbenchAdapter extends IpsObjectWorkbenchAdapter {

    // using imageRegistry and not the resource manager because images have to be leaded by input
    // stream to support all kinds of images in user projects, including images in archives.
    // The image registry uses the UIPlugin's resource manager internally
    private ImageRegistry imageRegistry;

    private ImageDescriptor prodCmptDefaultIcon;

    public ProductCmptWorkbenchAdapter() {
        super();
        prodCmptDefaultIcon = IpsUIPlugin.getImageHandling().createImageDescriptor("ProductCmpt.gif"); //$NON-NLS-1$
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
     * Public for testing purposes.
     */
    public IconDesc getProductCmptIconDesc(IProductCmptType type) {
        if (type == null) {
            return new DefaultIconDesc();
        }

        if (type.isUseCustomInstanceIcon()) {
            return new PathIconDesc(type.getIpsProject(), type.getInstancesIcon());
        } else if (type.hasSupertype()) {
            IProductCmptType superType;
            try {
                superType = (IProductCmptType)type.findSupertype(type.getIpsProject());
            } catch (CoreException e) {
                return new DefaultIconDesc();
            }
            return getProductCmptIconDesc(superType);
        } else {
            return new DefaultIconDesc();
        }
    }

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsSrcFile ipsSrcFile) {
        try {
            String typeName = ipsSrcFile.getPropertyValue(IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE);
            if (typeName != null) {
                IProductCmptType type = ipsSrcFile.getIpsProject().findProductCmptType(typeName);
                return getProductCmptImageDescriptor(type);
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return getDefaultImageDescriptor();
    }

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObject ipsObject) {
        try {
            if (ipsObject instanceof IProductCmpt) {
                IProductCmpt productCmpt = (IProductCmpt)ipsObject;
                return getProductCmptImageDescriptor(productCmpt.findProductCmptType(ipsObject.getIpsProject()));
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
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
