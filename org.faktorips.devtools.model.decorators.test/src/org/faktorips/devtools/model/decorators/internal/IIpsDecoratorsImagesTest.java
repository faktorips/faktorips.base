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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.junit.Test;

public class IIpsDecoratorsImagesTest extends AbstractIpsPluginTest {

    @Test
    public void testCreateImageDescriptor() {
        String name = "test";
        ImageDescriptor descriptor = IIpsDecorators.getImageHandling().createImageDescriptor(name);

        URL url = IpsModelIconsPluginActivator.getBundle().getEntry("icons/" + name); //$NON-NLS-1$
        assertEquals(ImageDescriptor.createFromURL(url), descriptor);
    }

    @Test
    public void testResourceHandling() {
        String name = "TestImage.gif";
        ImageDescriptor descriptor = ImageDescriptor.createFromFile(this.getClass(), name);
        ResourceManager rmA = new LocalResourceManager(JFaceResources.getResources());
        Image imageA = (Image)rmA.get(descriptor);
        assertFalse(imageA.isDisposed());
        rmA.dispose();
        assertTrue(imageA.isDisposed());
        rmA = new LocalResourceManager(JFaceResources.getResources());

        ResourceManager rmB = new LocalResourceManager(JFaceResources.getResources());

        imageA = (Image)rmA.get(descriptor);
        Image imageB = (Image)rmB.get(descriptor);
        assertEquals(imageA, imageB);
        Image imageP = IIpsDecorators.getImageHandling().getImage(descriptor);
        assertEquals(imageA, imageP);

        rmA.dispose();
        assertFalse(imageB.isDisposed());
        assertFalse(imageP.isDisposed());
        assertEquals(imageA, imageB);
        assertEquals(imageB, imageP);

        rmB.dispose();
        assertFalse(imageB.isDisposed());
        assertFalse(imageP.isDisposed());
        assertEquals(imageA, imageB);
        assertEquals(imageA, imageP);

        IIpsDecorators.getImageHandling().disposeImage(descriptor);
        assertTrue(imageB.isDisposed());
        assertTrue(imageP.isDisposed());
    }

    @Test
    public void testDisabledgetImage() throws Exception {
        IIpsElement ipsElement = newIpsProject();
        IIpsElement newIpsElement = newIpsProject("zwei");

        ImageDescriptor disabledDescriptor = IIpsDecorators.getImageHandling().getDisabledImageDescriptor(ipsElement);
        Image disabledImage = IIpsDecorators.getImageHandling().getDisabledImage(ipsElement);

        // test descriptors
        // simply call twice
        assertEquals(disabledDescriptor, IIpsDecorators.getImageHandling().getDisabledImageDescriptor(ipsElement));
        assertEquals(disabledDescriptor, IIpsDecorators.getImageHandling().getDisabledImageDescriptor(newIpsElement));

        // test images
        assertEquals(disabledImage, IIpsDecorators.getImageHandling().getDisabledImage(ipsElement));
        assertEquals(disabledImage, IIpsDecorators.getImageHandling().getDisabledImage(newIpsElement));

        IIpsDecorators.getImageHandling().disposeImage(disabledDescriptor);
        assertTrue(disabledImage.isDisposed());
    }

    /**
     * Testing the image methods in the IIpsDecorators by loading the same images several times. So
     * there should be only one instance of the real image.
     */
    @Test
    public void testImageLoadingStress() throws Exception {
        String imageName = "New.gif";
        Image expected = IIpsDecorators.getImageHandling().getSharedImage(imageName, true);

        // Test getSharedImage(String)
        for (int i = 0; i < 10000; i++) {
            Image actual = IIpsDecorators.getImageHandling().getSharedImage(imageName, true);
            assertEquals(expected, actual);
        }

        // Test get
        ImageDescriptor expDescriptor = IIpsDecorators.getImageHandling().getSharedImageDescriptor(imageName, true);
        Image newExpected = IIpsDecorators.getImageHandling().getImage(expDescriptor);
        for (int i = 0; i < 10000; i++) {
            ImageDescriptor descriptor = IIpsDecorators.getImageHandling().getSharedImageDescriptor(imageName, true);
            Image actual = IIpsDecorators.getImageHandling().getImage(descriptor);
            assertEquals(newExpected, actual);
            assertEquals(expected, actual);
        }

        ResourceManager manager = new LocalResourceManager(JFaceResources.getResources());
        for (int i = 0; i < 10000; i++) {
            Image actual = (Image)manager.get(expDescriptor);
            assertEquals(newExpected, actual);
            assertEquals(expected, actual);
        }
    }

    public void tesOverlayImages() throws Exception {
        ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());

        String baseName = "New.gif";
        Image baseImage = IIpsDecorators.getImageHandling().getSharedImage(baseName, true);

        ImageDescriptor ovr1 = OverlayIcons.ABSTRACT_OVR_DESC;
        ImageDescriptor ovr2 = OverlayIcons.OVERRIDE_OVR_DESC;
        ImageDescriptor ovr3 = OverlayIcons.ERROR_OVR_DESC;
        ImageDescriptor ovr4 = OverlayIcons.WARNING_OVR_DESC;

        ImageDescriptor expected = new DecorationOverlayIcon(baseImage,
                new ImageDescriptor[] { ovr1, ovr2, ovr3, ovr4 });

        ImageDescriptor actual = IIpsDecorators.getImageHandling().getSharedOverlayImageDescriptor(
                baseName,
                new String[] { OverlayIcons.ABSTRACT, OverlayIcons.OVERRIDE, OverlayIcons.ERROR,
                        OverlayIcons.WARNING });

        assertEquals(expected, actual);
        assertEquals(resourceManager.get(expected), IIpsDecorators.getImageHandling().getImage(actual));

        expected = new DecorationOverlayIcon(baseImage, ovr1, IDecoration.TOP_LEFT);
        actual = IIpsDecorators.getImageHandling().getSharedOverlayImageDescriptor(baseName, OverlayIcons.ABSTRACT,
                IDecoration.TOP_LEFT);

        assertEquals(expected, actual);
        assertEquals(resourceManager.get(expected), IIpsDecorators.getImageHandling().getImage(actual));
        resourceManager.dispose();
    }

}
