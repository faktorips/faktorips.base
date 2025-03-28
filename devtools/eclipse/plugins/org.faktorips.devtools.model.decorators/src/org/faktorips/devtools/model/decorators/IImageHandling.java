/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.decorators;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.decorators.internal.ImageHandling;

/**
 * Manages {@link ImageDescriptor ImageDescriptors} and {@link Image Images} with a
 * {@link ResourceManager}.
 *
 * @since 21.6
 */
public interface IImageHandling {

    /**
     * To get an image descriptor to a specified name. If the image descriptor is not already
     * registered in the plugin's image registry and the flag createIfAbsent is true, this method
     * does. Only use this method for images you want to share for the whole plugin.
     *
     * @see ImageHandling
     *
     * @param name the name of the image equal to the filename in the sub-folder icons
     *
     * @return the shared image descriptor
     */
    ImageDescriptor getSharedImageDescriptor(String name, boolean createIfAbsent);

    /**
     * To register an image descriptor in the image registry. The name of the image is the filename
     * in the sub-folder <em>icons</em> that means the path to the image is {bundle}/icons/name
     */
    void registerSharedImageDescriptor(String name, ImageDescriptor descriptor);

    /**
     * Get the shared descriptor for disable image with the descriptor of an enabled image
     */
    ImageDescriptor getDisabledImageDescriptor(IAdaptable adaptable);

    ImageDescriptor getDisabledImageDescriptor(ImageDescriptor enabledImageDescriptor);

    /**
     * Getting an image descriptor by calling the {@link IIpsElementDecorator} of the IPS element If
     * there is no registered adapter this method returns null. If the registered adapter has no
     * image, this method returns the missing image
     *
     * @return the image descriptor or null if there is no image or no registered adapter
     */
    ImageDescriptor getImageDescriptor(IAdaptable adaptable);

    /**
     * Get the default image descriptor for an IPS element class. May return {@code null}.
     * <p>
     * <em>Note:</em>The workbench adapters are registered for concrete implementations not for
     * interfaces
     */
    ImageDescriptor getDefaultImageDescriptor(Class<? extends IIpsElement> ipsElementClass);

    /**
     * Returns the image with the indicated name from the <code>icons</code> folder and overlays it
     * with the specified overlay image. If the given image is not found return the missing image
     * overlaid with the product relevant image.
     *
     * @param baseImageName The name of the image which will be overlaid with the overlay image.
     * @param overlayImageName The name of the overlay image
     * @param quadrant the quadrant where the overlay is painted, one of
     *            {@link IDecoration#TOP_LEFT} {@link IDecoration#TOP_RIGHT},
     *            {@link IDecoration#BOTTOM_LEFT} or {@link IDecoration#BOTTOM_RIGHT}
     */
    ImageDescriptor getSharedOverlayImageDescriptor(String baseImageName, String overlayImageName, int quadrant);

    /**
     * Returns the image with the indicated name from the <code>icons</code> folder and overlays it
     * with the specified overlay image. If the given image is not found return the missing image
     * overlaid with the product relevant image.
     *
     * @param sharedImageDescriptor The ImageDescriptor of the image which will be overlaid with the
     *            overlay image
     * @param overlayImageName The name of the overlay image
     * @param quadrant the quadrant where the overlay is painted, one of
     *            {@link IDecoration#TOP_LEFT} {@link IDecoration#TOP_RIGHT},
     *            {@link IDecoration#BOTTOM_LEFT} or {@link IDecoration#BOTTOM_RIGHT}
     */
    ImageDescriptor getSharedOverlayImageDescriptor(ImageDescriptor sharedImageDescriptor,
            String overlayImageName,
            int quadrant);

    /**
     * Returns the image with the indicated name from the <code>icons</code> folder and overlays it
     * with the specified overlay image. If the given image is not found return the missing image
     * overlaid with the product relevant image.
     *
     * @param baseImage The image which will be overlaid with the overlay image
     * @param overlayImageName The name of the overlay image
     * @param quadrant the quadrant where the overlay is painted, one of
     *            {@link IDecoration#TOP_LEFT} {@link IDecoration#TOP_RIGHT},
     *            {@link IDecoration#BOTTOM_LEFT} or {@link IDecoration#BOTTOM_RIGHT}
     */
    ImageDescriptor getSharedOverlayImageDescriptor(Image baseImage, String overlayImageName, int quadrant);

    /**
     * Returns the image with the indicated name from the <code>icons</code> folder and overlaid by
     * the specified overlay images. The array contains the names of the overlays, sorted in
     * following order: top-left, top-right, bottom-left, bottom-right. If the given image is not
     * found return the missing image overlaid with the product relevant image.
     *
     *
     * @param baseImageName The name of the image which will be overlaid with the overlay image.
     * @param overlayImageNames The names of the overlay images
     */
    ImageDescriptor getSharedOverlayImageDescriptor(String baseImageName, String[] overlayImageNames);

    /**
     * Just create a image descriptor with the specified name as image filename in the icons
     * sub-folder does not register anything in the image registry or the image description
     * registry. Only use for images of this plugin!
     * <p>
     * Use this method when you only want to have an image descriptor for any eclipse object e.g. an
     * Action or a Wizard Normally eclipse does instantiate and dispose the image
     *
     * @return the new created image descriptor
     */
    ImageDescriptor createImageDescriptor(String name);

    /**
     * ResourceManager handles correct allocation and disposal of resources. In general, you should
     * use a *Registry class to map IDs onto descriptors, and use a ResourceManager to convert the
     * descriptors into real Images/Fonts/etc.
     *
     * @return the {@link ResourceManager}
     */
    ResourceManager getResourceManager();

    /**
     * To get an image for an image descriptor from resource manager. If no such resource already
     * exists the resource manager creates a new one. The image will remain allocated for the
     * lifetime of the plugin. If the image is not potentially needed by other classes use the
     * methods {@link #createImage(ImageDescriptor)} and {@link #disposeImage(ImageDescriptor)} or
     * even better use your own LocalResourceManager.
     * <p>
     * If descriptor is {@code null} the missing image is returned
     */
    Image getImage(ImageDescriptor descriptor);

    /**
     * To get an image for an image descriptor from resource manager. If no such resource already
     * exists the resource manager creates a new one. The image will remain allocated for the
     * lifetime of the plugin. If the image is not potentially needed by other classes use the
     * methods {@link #createImage(ImageDescriptor)} and {@link #disposeImage(ImageDescriptor)} or
     * even better use your own LocalResourceManager.
     *
     * @param returnMissingImage if true, the MissingImage is returned instead of null
     */
    Image getImage(ImageDescriptor descriptor, boolean returnMissingImage);

    /**
     * Create an image in the resource manager. You have to dispose the image by calling
     * {@link #disposeImage(ImageDescriptor)} if you do not need it any longer. If you want to share
     * the image with other components, use one of the shared image methods. If the image descriptor
     * is already registered as a shared image, the descriptor is not registered twice. You do not
     * have to worry about calling the {@link #disposeImage(ImageDescriptor)} method because a
     * shared image also would not be disposed
     */
    Image createImage(ImageDescriptor descriptor);

    /**
     * To dispose a self registered image. Do not dispose shared images (in fact this method
     * wouldn't do).
     *
     * @param descriptor the descriptor of the image you want to dispose
     *
     */
    void disposeImage(ImageDescriptor descriptor);

    /**
     * Get the default image for an IPS element class. May return null. Note: The workbench adapters
     * are registered for concrete implementations not for interfaces
     */
    Image getDefaultImage(Class<? extends IIpsElement> ipsElementClass);

    /**
     * Getting the image for an IPS element by calling the {@link IIpsDecorators} for the specified
     * IPS element. The image is either a shared image (if someone already registered the
     * corresponding image descriptor) or a not shared one if no one registered the image before. If
     * it is a no shared image, someone (maybe you - normally the workbench adapter) have dispose
     * the image.
     */
    Image getImage(IAdaptable adaptable);

    /**
     * Get the enabled or disabled image for the given element.
     *
     * @see #getImage(IAdaptable)
     * @see #getDisabledImage(IAdaptable)
     */
    Image getImage(IAdaptable adaptable, boolean enabled);

    /**
     * Get the disabled version of a shared image for an IPS element
     */
    Image getDisabledImage(IAdaptable adaptable);

    /**
     * Returns the image with the indicated name from the <code>icons</code> folder. If no image
     * with the indicated name is found and createIfAbsent is false null is returned.
     *
     * @param name The image name, e.g. <code>IpsProject.gif</code>
     * @param createIfAbsent true to create a new image if not already registered
     */
    Image getSharedImage(String name, boolean createIfAbsent);

    /**
     * Return a shared image which is the disabled version of the given image descriptor
     */
    Image getDisabledSharedImage(ImageDescriptor enabledImage);

}
