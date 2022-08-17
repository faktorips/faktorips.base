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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsElementDecorator;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.junit.Test;

public class ProductCmptTypeMethodDecoratorTest extends AbstractIpsPluginTest {

    @Test
    public void testGetImageDescriptor_Null() {
        ProductCmptTypeMethodDecorator decorator = new ProductCmptTypeMethodDecorator();

        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(null);

        assertThat(imageDescriptor, is(decorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor() {
        IProductCmptTypeMethod method = mock(IProductCmptTypeMethod.class);

        IIpsElementDecorator decorator = new ProductCmptTypeMethodDecorator();

        when(method.isChangingOverTime()).thenReturn(true, false);
        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(method);
        assertNotNull(imageDescriptor);
        ImageDescriptor privateImageDescriptor = createImageDescriptorWithChangingOverTimeOverlay(true);
        assertTrue(privateImageDescriptor.equals(imageDescriptor));

        imageDescriptor = decorator.getImageDescriptor(method);
        assertNotNull(imageDescriptor);
        privateImageDescriptor = createImageDescriptorWithChangingOverTimeOverlay(false);
        assertTrue(privateImageDescriptor.equals(imageDescriptor));
    }

    private ImageDescriptor createImageDescriptorWithChangingOverTimeOverlay(boolean changingOverTime) {
        String baseImage = ProductCmptTypeMethodDecorator.METHOD_IMAGE_NAME;
        String[] overlays = new String[4];
        if (!changingOverTime) {
            overlays[0] = OverlayIcons.STATIC;
        }
        return IIpsDecorators.getImageHandling().getSharedOverlayImageDescriptor(baseImage, overlays);
    }

}
