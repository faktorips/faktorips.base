/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ValidationRuleWBAdapterTest extends AbstractIpsPluginTest {

    private IValidationRule rule;
    private ValidationRuleWorkbenchAdapter adapter;
    private String[] overlays = new String[4];

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        rule = mock(IValidationRule.class);
        when(rule.getName()).thenReturn("RegelName");
        when(rule.isConfigurableByProductComponent()).thenReturn(false);

        adapter = new ValidationRuleWorkbenchAdapter();
    }

    @Test
    public void testGetImage() {
        ImageDescriptor imageDesc = adapter.getImageDescriptor(rule);
        ImageDescriptor expectedImageDesc = IpsUIPlugin.getImageHandling()
                .getSharedImageDescriptor(ValidationRuleWorkbenchAdapter.VALIDATION_RULE_DEF_BASE_IMAGE, true);
        assertEquals(expectedImageDesc, imageDesc);
    }

    @Test
    public void testGetConfigurableImage_withChangingOverTime() {
        when(rule.isConfigurableByProductComponent()).thenReturn(true);
        when(rule.isChangingOverTime()).thenReturn(true);

        assertEquals(createImageDescriptorWithOverlays(true, true), adapter.getImageDescriptor(rule));
    }

    @Test
    public void testGetConfigurableImage_withoutChangingOverTime() {
        when(rule.isConfigurableByProductComponent()).thenReturn(true);
        when(rule.isChangingOverTime()).thenReturn(false);

        assertEquals(createImageDescriptorWithOverlays(true, false), adapter.getImageDescriptor(rule));
    }

    @Test
    @Ignore
    public void testGetLabel() {
        assertEquals("RegelName", adapter.getLabel(rule));
    }

    private ImageDescriptor createImageDescriptorWithOverlays(boolean productRelevant, boolean nonChangingOverTime) {
        if (productRelevant) {
            overlays[1] = OverlayIcons.PRODUCT_OVR;
            if (!nonChangingOverTime) {
                overlays[0] = OverlayIcons.NOT_CHANGEOVERTIME_OVR;
            }
        }
        return IpsUIPlugin.getImageHandling()
                .getSharedOverlayImage(ValidationRuleWorkbenchAdapter.VALIDATION_RULE_DEF_BASE_IMAGE, overlays);
    }

}
