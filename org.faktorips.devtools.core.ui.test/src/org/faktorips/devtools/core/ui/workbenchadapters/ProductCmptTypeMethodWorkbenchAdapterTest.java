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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;
import org.junit.Test;

public class ProductCmptTypeMethodWorkbenchAdapterTest extends AbstractIpsPluginTest {

    @Test
    public void testGetImageDescriptor() {
        IProductCmptTypeMethod method = mock(IProductCmptTypeMethod.class);

        IWorkbenchAdapter superAdapter = new ProductCmptTypeMethodWorkbenchAdapter();

        when(method.isChangingOverTime()).thenReturn(true, false);
        ImageDescriptor imageDescriptor = superAdapter.getImageDescriptor(method);
        assertNotNull(imageDescriptor);
        ImageDescriptor privateImageDescriptor = createImageDescriptorWithChangingOverTimeOverlay(true);
        assertTrue(privateImageDescriptor.equals(imageDescriptor));

        imageDescriptor = superAdapter.getImageDescriptor(method);
        assertNotNull(imageDescriptor);
        privateImageDescriptor = createImageDescriptorWithChangingOverTimeOverlay(false);
        assertTrue(privateImageDescriptor.equals(imageDescriptor));
    }

    private ImageDescriptor createImageDescriptorWithChangingOverTimeOverlay(boolean changingOverTime) {
        String baseImage = ProductCmptTypeMethodWorkbenchAdapter.METHOD_IMAGE_NAME;
        String[] overlays = new String[4];
        if (!changingOverTime) {
            overlays[0] = OverlayIcons.NOT_CHANGEOVERTIME_OVR;
        }
        return IpsUIPlugin.getImageHandling().getSharedOverlayImage(baseImage, overlays);
    }

}
