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
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Test;

public class IpsUIPluginImagesTest extends AbstractIpsPluginTest {

    @Test
    public void testCreateImageDescriptor() {
        String name = "test";
        ImageDescriptor descriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(name);

        URL url = IpsUIPlugin.getDefault().getBundle().getEntry("icons/" + name); //$NON-NLS-1$
        assertEquals(ImageDescriptor.createFromURL(url), descriptor);
    }

    @Test
    public void testRegisterAndDisposeImage() {
        String name = "TestCase.gif";
        ImageDescriptor expDescriptor = ImageDescriptor.createFromFile(this.getClass(), name);
        Image expImage = IpsUIPlugin.getImageHandling().createImage(expDescriptor);
        assertEquals(expImage, IpsUIPlugin.getImageHandling().getImage(expDescriptor));

        IpsUIPlugin.getImageHandling().disposeImage(expDescriptor);
        assertTrue(expImage.isDisposed());

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
        Image reImage = (Image)otherManager.get(sharedDesc);
        assertFalse(reImage.isDisposed());
        assertFalse(sharedImage.equals(reImage));

        Image reSharedImage = IpsUIPlugin.getImageHandling().getImage(sharedDesc);
        assertEquals(reSharedImage, reImage);

        otherManager.dispose();
    }

    @Test
    public void testResourceHandling() {
        String name = "TestCase.gif";
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
    }

    @Test
    public void testDisabledgetImage() throws Exception {
        IIpsElement ipsElement = newIpsProject();
        IIpsElement newIpsElement = newIpsProject("zwei");

        ImageDescriptor disabledDescriptor = IpsUIPlugin.getImageHandling().getDisabledImageDescriptor(ipsElement);
        Image disabledImage = IpsUIPlugin.getImageHandling().getDisabledImage(ipsElement);

        // test descriptors
        // simply call twice
        assertEquals(disabledDescriptor, IpsUIPlugin.getImageHandling().getDisabledImageDescriptor(ipsElement));
        assertEquals(disabledDescriptor, IpsUIPlugin.getImageHandling().getDisabledImageDescriptor(newIpsElement));

        // test images
        assertEquals(disabledImage, IpsUIPlugin.getImageHandling().getDisabledImage(ipsElement));
        assertEquals(disabledImage, IpsUIPlugin.getImageHandling().getDisabledImage(newIpsElement));
    }

    @Test
    public void testIpsElementgetImage() throws Exception {
        IIpsProject testElement = newIpsProject();

        String elementImageName = "IpsProject.gif";
        ImageDescriptor elementImageDecriptor = IpsUIPlugin.getImageHandling().getSharedImageDescriptor(
                elementImageName, true);
        Image elementImage = IpsUIPlugin.getImageHandling().getSharedImage(elementImageName, true);

        assertEquals(elementImageDecriptor, IpsUIPlugin.getImageHandling().getImageDescriptor(testElement));
        assertEquals(elementImage, IpsUIPlugin.getImageHandling().getImage(testElement));

        WorkbenchLabelProvider labelProvider = new WorkbenchLabelProvider();
        Image actualImage = labelProvider.getImage(testElement);
        assertEquals(elementImage, actualImage);
        labelProvider.dispose();
        assertEquals(elementImage, actualImage);
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
            Image actual = (Image)manager.get(expDescriptor);
            assertEquals(newExpected, actual);
            assertEquals(expected, actual);
        }
    }

    public void tesOverlayImages() throws Exception {
        ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());

        String baseName = "New.gif";
        Image baseImage = IpsUIPlugin.getImageHandling().getSharedImage(baseName, true);

        ImageDescriptor ovr1 = OverlayIcons.ABSTRACT_OVR_DESC;
        ImageDescriptor ovr2 = OverlayIcons.OVERRIDE_OVR_DESC;
        ImageDescriptor ovr3 = OverlayIcons.ERROR_OVR_DESC;
        ImageDescriptor ovr4 = OverlayIcons.WARNING_OVR_DESC;

        ImageDescriptor expected = new DecorationOverlayIcon(baseImage,
                new ImageDescriptor[] { ovr1, ovr2, ovr3, ovr4 });

        ImageDescriptor actual = IpsUIPlugin.getImageHandling().getSharedOverlayImage(
                baseName,
                new String[] { OverlayIcons.ABSTRACT_OVR, OverlayIcons.OVERRIDE_OVR, OverlayIcons.ERROR_OVR,
                        OverlayIcons.WARNING_OVR });

        assertEquals(expected, actual);
        assertEquals(resourceManager.get(expected), IpsUIPlugin.getImageHandling().getImage(actual));

        expected = new DecorationOverlayIcon(baseImage, ovr1, IDecoration.TOP_LEFT);
        actual = IpsUIPlugin.getImageHandling().getSharedOverlayImage(baseName, OverlayIcons.ABSTRACT_OVR,
                IDecoration.TOP_LEFT);

        assertEquals(expected, actual);
        assertEquals(resourceManager.get(expected), IpsUIPlugin.getImageHandling().getImage(actual));
        resourceManager.dispose();
    }

}
