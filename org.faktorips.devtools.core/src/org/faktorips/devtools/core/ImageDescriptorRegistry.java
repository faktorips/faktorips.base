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

package org.faktorips.devtools.core;

import java.util.HashMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.faktorips.util.ArgumentCheck;

/**
 * A registry that maps <code>ImageDescriptor</code>s to <code>Image</code>s.
 */
public class ImageDescriptorRegistry {

    private HashMap<ImageDescriptor, Image> registry = new HashMap<ImageDescriptor, Image>(10);
    private Display display;

    /**
     * Creates a new image descriptor registry for the current or default display, respectively.
     */
    public ImageDescriptorRegistry() {
        this(getDisplay());
    }

    /**
     * Helper-Method to get the current or default display
     * 
     * @return The current display or the default display when no current display is available.
     */
    private static Display getDisplay() {
        Display display = Display.getCurrent();
        if (display == null) {
            display = Display.getDefault();
        }
        return display;
    }

    /**
     * Creates a new image descriptor registry for the given display. All images managed by this
     * registry will be disposed when the display gets disposed.
     * 
     * @param diaplay the display the images managed by this registry are allocated for
     */
    public ImageDescriptorRegistry(Display display) {
        ArgumentCheck.notNull(display);
        this.display = display;
        hookDisplay();
    }

    /**
     * Returns the image assiciated with the given image descriptor.
     * 
     * @param descriptor the image descriptor for which the registry manages an image
     * @return the image associated with the image descriptor or <code>null</code> if the image
     *         descriptor can't create the requested image.
     */
    public Image get(ImageDescriptor descriptor) {
        if (descriptor == null) {
            descriptor = ImageDescriptor.getMissingImageDescriptor();
        }
        Image result = registry.get(descriptor);
        if (result != null) {
            return result;
        }
        result = descriptor.createImage();
        if (result != null) {
            registry.put(descriptor, result);
        }
        return result;
    }

    /**
     * Disposes all images managed by this registry.
     */
    public void dispose() {
        for (Image image : registry.values()) {
            image.dispose();
        }
        registry.clear();
    }

    private void hookDisplay() {
        display.disposeExec(new Runnable() {
            public void run() {
                dispose();
            }
        });
    }

}
