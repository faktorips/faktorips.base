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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TableStructureUsageDecoratorTest extends AbstractIpsPluginTest {

    @Test
    public void testGetImageDescriptor() {
        ITableStructureUsage tableStructureUsage = mock(ITableStructureUsage.class);

        TableStructureUsageDecorator decorator = new TableStructureUsageDecorator();

        when(tableStructureUsage.isChangingOverTime()).thenReturn(true, false);
        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(tableStructureUsage);
        assertNotNull(imageDescriptor);
        ImageDescriptor privateImageDescriptor = createImageDescriptorWithChangingOverTimeOverlay(true);
        assertTrue(privateImageDescriptor.equals(imageDescriptor));

        imageDescriptor = decorator.getImageDescriptor(tableStructureUsage);
        assertNotNull(imageDescriptor);
        privateImageDescriptor = createImageDescriptorWithChangingOverTimeOverlay(false);
        assertTrue(privateImageDescriptor.equals(imageDescriptor));
    }

    private ImageDescriptor createImageDescriptorWithChangingOverTimeOverlay(boolean changingOverTime) {
        String baseImage = TableStructureUsageDecorator.BASE_IMAGE;
        String[] overlays = new String[4];
        if (!changingOverTime) {
            overlays[0] = OverlayIcons.NOT_CHANGEOVERTIME_OVR;
        }
        return IpsDecorators.getImageHandling().getSharedOverlayImage(baseImage, overlays);
    }
}
