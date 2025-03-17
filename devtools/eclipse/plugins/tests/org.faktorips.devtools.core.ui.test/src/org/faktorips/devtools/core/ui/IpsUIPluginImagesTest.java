/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;

public class IpsUIPluginImagesTest extends AbstractIpsPluginTest {

    @Test
    public void testCreateImageDescriptor() {
        String name = "test";
        ImageDescriptor descriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(name);

        URL url = IpsUIPlugin.getDefault().getBundle().getEntry("icons/" + name); //$NON-NLS-1$
        assertEquals(ImageDescriptor.createFromURL(url), descriptor);
    }

    private static boolean eclipseUsesImageCache() {
        // 3.24.0+ (Eclipse 2021-12) caches 300 images, see
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=577481
        return FrameworkUtil.getBundle(LocalResourceManager.class).getVersion()
                .compareTo(Version.valueOf("3.24.0")) >= 0;
    }

    @Test
    public void testRegisterAndDisposeImage() {
        String name = "TestImage.gif";
        ImageDescriptor expDescriptor = ImageDescriptor.createFromFile(this.getClass(), name);
        Image expImage = IpsUIPlugin.getImageHandling().createImage(expDescriptor);
        assertEquals(expImage, IpsUIPlugin.getImageHandling().getImage(expDescriptor));

        IpsUIPlugin.getImageHandling().disposeImage(expDescriptor);
        if (!eclipseUsesImageCache()) {
            assertTrue(expImage.isDisposed());
        } else {
            // no assertion, as disposed state depends on how full the cache is
        }

        // register the image as shared image
        ImageDescriptor sharedDesc = IpsUIPlugin.getImageHandling().getSharedImageDescriptor(name, true);
        Image sharedImage = IpsUIPlugin.getImageHandling().getImage(sharedDesc);
        assertEquals(sharedImage, IpsUIPlugin.getImageHandling().getResourceManager().find(sharedDesc));
        ResourceManager otherManager = new LocalResourceManager(JFaceResources.getResources());
        assertEquals(sharedImage, otherManager.get(sharedDesc));

        // this should NOT dispose the image because disposeImage(..) should only dispose
        // non shared images
        IpsUIPlugin.getImageHandling().disposeImage(sharedDesc);
        assertEquals(sharedImage, otherManager.find(sharedDesc));

        otherManager.dispose();
        assertNull(otherManager.find(sharedDesc));
        assertTrue(sharedImage.isDisposed());

        // getting an image from an already disposed resource manager should reactivate the resource
        // manager
        Image reImage = otherManager.get(sharedDesc);
        assertFalse(reImage.isDisposed());
        assertFalse(sharedImage.equals(reImage));

        Image reSharedImage = IpsUIPlugin.getImageHandling().getImage(sharedDesc);
        assertEquals(reSharedImage, reImage);

        otherManager.dispose();

        assertFalse(reImage.isDisposed());
        assertFalse(reSharedImage.isDisposed());

        IpsUIPlugin.getImageHandling().disposeImage(sharedDesc);

        assertTrue(reImage.isDisposed());
        assertTrue(reSharedImage.isDisposed());
    }

    @Test
    public void testIpsElementgetImage() throws Exception {
        IIpsProject testElement = newIpsProject();

        String elementImageName = "IpsProject.gif";
        ImageDescriptor elementImageDecriptor = IIpsDecorators.getImageHandling().getSharedImageDescriptor(
                elementImageName, true);
        Image elementImage = IIpsDecorators.getImageHandling().getSharedImage(elementImageName, true);

        assertEquals(elementImageDecriptor, IIpsDecorators.getImageHandling().getImageDescriptor(testElement));
        assertEquals(elementImage, IIpsDecorators.getImageHandling().getImage(testElement));

        WorkbenchLabelProvider labelProvider = new WorkbenchLabelProvider();
        Image actualImage = labelProvider.getImage(testElement);
        assertEquals(elementImage, actualImage);
        labelProvider.dispose();
        assertEquals(elementImage, actualImage);
    }

    @Test
    public void testResourceHandling() {
        String name = "TestImage.gif";
        ImageDescriptor descriptor = ImageDescriptor.createFromFile(this.getClass(), name);
        ResourceManager rmA = new LocalResourceManager(JFaceResources.getResources());
        Image imageA = rmA.get(descriptor);
        assertFalse(imageA.isDisposed());
        rmA.dispose();
        if (!eclipseUsesImageCache()) {
            assertTrue(imageA.isDisposed());
        }
        rmA = new LocalResourceManager(JFaceResources.getResources());

        ResourceManager rmB = new LocalResourceManager(JFaceResources.getResources());

        imageA = rmA.get(descriptor);
        Image imageB = rmB.get(descriptor);
        assertEquals(imageA, imageB);
        Image imageP = IpsUIPlugin.getImageHandling().getImage(descriptor);
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

        IpsUIPlugin.getImageHandling().disposeImage(descriptor);
        if (!eclipseUsesImageCache()) {
            assertTrue(imageB.isDisposed());
            assertTrue(imageP.isDisposed());
        }
    }

    /**
     * Testing the image methods in the IpsUIPlugin by loading the same images several times. So
     * there should be only one instance of the real image.
     */
    @Test
    public void testImageLoadingStress() throws Exception {
        String imageName = "New.gif";
        Image expected = IpsUIPlugin.getImageHandling().getSharedImage(imageName, true);

        // Test getSharedImage(String)
        for (int i = 0; i < 10000; i++) {
            Image actual = IpsUIPlugin.getImageHandling().getSharedImage(imageName, true);
            assertEquals(expected, actual);
        }

        // Test get
        ImageDescriptor expDescriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor(imageName, true);
        Image newExpected = IpsUIPlugin.getImageHandling().getImage(expDescriptor);
        for (int i = 0; i < 10000; i++) {
            ImageDescriptor descriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor(imageName, true);
            Image actual = IpsUIPlugin.getImageHandling().getImage(descriptor);
            assertEquals(newExpected, actual);
            assertEquals(expected, actual);
        }

        ResourceManager manager = new LocalResourceManager(JFaceResources.getResources());
        for (int i = 0; i < 10000; i++) {
            Image actual = manager.get(expDescriptor);
            assertEquals(newExpected, actual);
            assertEquals(expected, actual);
        }
    }
}
