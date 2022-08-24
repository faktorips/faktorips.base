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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.junit.Test;

public class ImageHandlingTest {

    private static final String LINK_GIF = "LinkOverlay.gif";

    private static final String PRODUCT_GIF = "ProductCmptType.gif";

    @Test
    public void testGetSharedOverlayImage() throws Exception {
        ImageDescriptor sharedOverlayImage = IIpsDecorators.getImageHandling().getSharedOverlayImageDescriptor(
                PRODUCT_GIF,
                LINK_GIF,
                IDecoration.BOTTOM_LEFT);
        Image sharedImage = IIpsDecorators.getImageHandling().getSharedImage(PRODUCT_GIF, false);
        ImageDescriptor sharedOverlayByImage = IIpsDecorators.getImageHandling()
                .getSharedOverlayImageDescriptor(sharedImage, LINK_GIF, IDecoration.BOTTOM_LEFT);

        assertNotNull(sharedOverlayImage);
        assertSame(sharedOverlayByImage, sharedOverlayImage);
    }

    @Test
    public void testGetSharedOverlayImage_Empty() throws Exception {
        ImageDescriptor sharedOverlayImage = IIpsDecorators.getImageHandling().getSharedOverlayImageDescriptor(
                IpsStringUtils.EMPTY, IpsStringUtils.EMPTY, IDecoration.BOTTOM_LEFT);

        assertNotNull(sharedOverlayImage);
        assertSame(IIpsDecorators.getImageHandling().getSharedImageDescriptor(IpsStringUtils.EMPTY, true),
                sharedOverlayImage);
    }

    @Test
    public void testGetSharedOverlayImageDescriptor() throws Exception {
        ImageDescriptor prodCmptTypeGif = IIpsDecorators.getImageHandling().createImageDescriptor(PRODUCT_GIF);
        Image baseImage = IIpsDecorators.getImageHandling().createImage(prodCmptTypeGif);

        ImageDescriptor resultImageD = IIpsDecorators.getImageHandling().getSharedOverlayImageDescriptor(baseImage,
                LINK_GIF, IDecoration.BOTTOM_LEFT);
        ImageDescriptor overlayedImageDescriptor = IIpsDecorators.getImageHandling()
                .getSharedImageDescriptor(LINK_GIF + "_" + baseImage.hashCode(), false);

        assertNotNull(resultImageD);
        assertSame(overlayedImageDescriptor, resultImageD);
    }

}
