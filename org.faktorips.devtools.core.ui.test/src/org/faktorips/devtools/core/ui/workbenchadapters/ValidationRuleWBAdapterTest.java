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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ValidationRuleWBAdapterTest extends AbstractIpsPluginTest {

    private IValidationRule rule;
    private ValidationRuleWorkbenchAdapter adapter;

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

        ImageDescriptor expectedImageDesc = IpsUIPlugin.getImageHandling().getSharedImageDescriptor(
                "ValidationRuleDef.gif", true);
        assertEquals(expectedImageDesc, imageDesc);
    }

    @Test
    public void testGetConfigurableImage() {
        when(rule.isConfigurableByProductComponent()).thenReturn(true);

        ImageDescriptor imageDesc = adapter.getImageDescriptor(rule);

        ImageDescriptor expectedImageDesc = IpsUIPlugin.getImageHandling().getSharedOverlayImage(
                "ValidationRuleDef.gif", "ProductRelevantOverlay.gif", IDecoration.TOP_RIGHT);
        assertEquals(expectedImageDesc, imageDesc);
    }

    @Test
    @Ignore
    public void testGetLabel() {
        assertEquals("RegelName", adapter.getLabel(rule));
    }

}
