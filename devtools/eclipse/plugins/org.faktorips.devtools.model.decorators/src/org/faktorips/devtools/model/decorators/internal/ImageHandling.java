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

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.decorators.IImageHandling;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.osgi.framework.Bundle;

/**
 * Images in eclipse is not so easy as it looks like. If you are not familiar with the basics of
 * image handling in eclipse, read this short article <a href=
 * "http://www.eclipse.org/articles/Article-Using%20Images%20In%20Eclipse/Using%20Images%20In%20Eclipse.html"
 * >Using Images in the Eclipse UI</a>
 * <p>
 * In Faktor-IPS we have a two kinds of images handled by the image handling. The first kind of
 * image is a plugin shared image. Only use shared images for those icons that are really important
 * for several components of the plugin and more important, those that do not change over time.
 * <p>
 * The second kind of images are not shared images.
 */
public class ImageHandling implements IImageHandling {

    private static final Map<ImageDescriptor, ImageDescriptor> ENABLE_DISABLE_MAP = new HashMap<>();

    private ResourceManager resourceManager;

    /**
     * used to map image names (also composit names for overlays) to descriptors
     */
    private Map<String, ImageDescriptor> descriptorMap = new HashMap<>();

    private final Bundle bundle;

    public ImageHandling(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public ResourceManager getResourceManager() {
        if (resourceManager == null) {
            resourceManager = createResourceManager();
        }
        return resourceManager;
    }

    private ResourceManager createResourceManager() {
        // If we are in the UI Thread use that
        if (Display.getCurrent() != null) {
            return new LocalResourceManager(JFaceResources.getResources());
        }
        // Use the default display if it is not the UI Thread.
        return new LocalResourceManager(JFaceResources.getResources(Display.getDefault()));
    }

    @Override
    public ImageDescriptor getSharedImageDescriptor(String name, boolean createIfAbsent) {
        ImageDescriptor descriptor = descriptorMap.get(name);
        if (descriptor == null && createIfAbsent) {
            descriptor = createImageDescriptor(name);
            registerSharedImageDescriptor(name, descriptor);
        }
        return descriptor;
    }

    @Override
    public void registerSharedImageDescriptor(String name, ImageDescriptor descriptor) {
        if (descriptor != null && descriptor != ImageDescriptor.getMissingImageDescriptor()) {
            descriptorMap.put(name, descriptor);
        }
    }

    @Override
    public ImageDescriptor getDisabledImageDescriptor(IAdaptable adaptable) {
        ImageDescriptor enabledImageDescriptor = getImageDescriptor(adaptable);
        return getDisabledImageDescriptor(enabledImageDescriptor);
    }

    @Override
    public ImageDescriptor getDisabledImageDescriptor(ImageDescriptor enabledImageDescriptor) {
        return ENABLE_DISABLE_MAP.computeIfAbsent(enabledImageDescriptor, this::createDisabledImageDescriptor);
    }

    /**
     * Create the disabled version of a shared image descriptor
     */
    private ImageDescriptor createDisabledImageDescriptor(ImageDescriptor enabledImageDescriptor) {
        return ImageDescriptor.createWithFlags(enabledImageDescriptor, SWT.IMAGE_DISABLE);
    }

    @Override
    public ImageDescriptor createImageDescriptor(String name) {
        URL url = bundle.getEntry("icons/" + name); //$NON-NLS-1$
        return ImageDescriptor.createFromURL(url);
    }

    @Override
    public ImageDescriptor getImageDescriptor(IAdaptable adaptable) {
        if (adaptable == null) {
            return getSharedImageDescriptor("IpsElement_broken.gif", true); //$NON-NLS-1$
        }
        if (adaptable instanceof IIpsElement ipsElement) {
            ImageDescriptor descriptor = IIpsDecorators.getImageDescriptor(ipsElement);
            if (descriptor != null) {
                return descriptor;
            } else {
                return ImageDescriptor.getMissingImageDescriptor();
            }
        }
        return null;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor(Class<? extends IIpsElement> ipsElementClass) {
        return IIpsDecorators.get(ipsElementClass).getDefaultImageDescriptor();
    }

    @Override
    public ImageDescriptor getSharedOverlayImageDescriptor(String baseImageName,
            String overlayImageName,
            int quadrant) {
        if (IpsStringUtils.isEmpty(overlayImageName)) {
            return getSharedImageDescriptor(baseImageName, true);
        }
        Image baseImage = getSharedImage(baseImageName, true);
        return getSharedOverlayImageDescriptor(baseImage, overlayImageName, quadrant);
    }

    @Override
    public ImageDescriptor getSharedOverlayImageDescriptor(ImageDescriptor sharedImageDescriptor,
            String overlayImageName,
            int quadrant) {
        return getSharedOverlayImageDescriptor(getImage(sharedImageDescriptor), overlayImageName, quadrant);
    }

    @Override
    public ImageDescriptor getSharedOverlayImageDescriptor(Image baseImage, String overlayImageName, int quadrant) {
        String overlayedImageName = overlayImageName + "_" + baseImage.hashCode(); //$NON-NLS-1$
        ImageDescriptor imageDescriptor = getSharedImageDescriptor(overlayedImageName, false);
        if (imageDescriptor == null) {
            ImageDescriptor overlay = createImageDescriptor(overlayImageName);
            imageDescriptor = new DecorationOverlayIcon(baseImage, overlay, quadrant);
            registerSharedImageDescriptor(overlayedImageName, imageDescriptor);
        }
        return imageDescriptor;
    }

    @Override
    public ImageDescriptor getSharedOverlayImageDescriptor(String baseImageName, String[] overlayImageNames) {
        String overlayedImageName = Arrays.toString(overlayImageNames) + "_" + baseImageName; //$NON-NLS-1$
        ImageDescriptor imageDescriptor = getSharedImageDescriptor(overlayedImageName, false);
        if (imageDescriptor == null) {
            Image baseImage = getSharedImage(baseImageName, true);
            ImageDescriptor[] overlays = new ImageDescriptor[overlayImageNames.length];
            for (int i = 0; i < overlayImageNames.length; i++) {
                if (overlayImageNames[i] != null) {
                    overlays[i] = createImageDescriptor(overlayImageNames[i]);
                }
            }
            imageDescriptor = new DecorationOverlayIcon(baseImage, overlays);
            registerSharedImageDescriptor(overlayedImageName, imageDescriptor);
        }
        return imageDescriptor;
    }

    @Override
    public Image getSharedImage(String name, boolean createIfAbsent) {
        ImageDescriptor descriptor = getSharedImageDescriptor(name, createIfAbsent);
        if (createIfAbsent) {
            return getImage(descriptor);
        } else {
            if (descriptor != null) {
                return getResourceManager().find(descriptor);
            } else {
                return null;
            }
        }
    }

    @Override
    public Image getDisabledSharedImage(ImageDescriptor enabledImage) {
        ImageDescriptor disabledID = getDisabledImageDescriptor(enabledImage);
        return getImage(disabledID);
    }

    @Override
    public Image getImage(ImageDescriptor descriptor) {
        return getImage(descriptor, true);
    }

    @Override
    public Image getImage(ImageDescriptor descriptor, boolean returnMissingImage) {
        if (descriptor != null) {
            return getResourceManager().get(descriptor);
        }
        if (returnMissingImage) {
            return getResourceManager().get(ImageDescriptor.getMissingImageDescriptor());
        }
        return null;
    }

    @Override
    public Image createImage(ImageDescriptor descriptor) {
        if (descriptor != null) {
            return getResourceManager().create(descriptor);
        }
        return getResourceManager().get(ImageDescriptor.getMissingImageDescriptor());
    }

    @Override
    public void disposeImage(ImageDescriptor descriptor) {
        getResourceManager().destroy(descriptor);
    }

    @Override
    public Image getDefaultImage(Class<? extends IIpsElement> ipsElementClass) {
        ImageDescriptor descriptor = getDefaultImageDescriptor(ipsElementClass);
        if (descriptor != null) {
            return getImage(descriptor);
        } else {
            return null;
        }
    }

    @Override
    public Image getImage(IAdaptable adaptable) {
        return getImage(getImageDescriptor(adaptable), false);
    }

    @Override
    public Image getImage(IAdaptable adaptable, boolean enabled) {
        if (enabled) {
            return getImage(adaptable);
        } else {
            return getDisabledImage(adaptable);
        }
    }

    @Override
    public Image getDisabledImage(IAdaptable adaptable) {
        return getImage(getDisabledImageDescriptor(adaptable));
    }

    public void dispose() {
        if (resourceManager != null) {
            resourceManager.dispose();
        }
    }
}
