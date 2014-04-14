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
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Stefan Widmaier
 */
public class ValidationRuleConfigWBAdapterTest extends AbstractIpsPluginTest {

    private IValidationRuleConfig config;
    private ValidationRuleConfigWorkbenchAdapter adapter;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        config = mock(IValidationRuleConfig.class);
        when(config.getName()).thenReturn("RegelName");
        when(config.isActive()).thenReturn(true);
        adapter = new ValidationRuleConfigWorkbenchAdapter();
    }

    @Test
    public void testGetActiveImage() {
        ImageDescriptor imageDesc = adapter.getImageDescriptor(config);

        ImageDescriptor expectedImageDesc = IpsUIPlugin.getImageHandling().getSharedImageDescriptor(
                "ValidationRuleDef.gif", true);
        assertEquals(expectedImageDesc, imageDesc);
    }

    @Test
    public void testGetInactiveImage() {
        when(config.isActive()).thenReturn(false);

        ImageDescriptor imageDesc = adapter.getImageDescriptor(config);

        ImageDescriptor expectedImageDesc = IpsUIPlugin.getImageHandling().getSharedImageDescriptor(
                "ValidationRuleDef.gif", true);
        expectedImageDesc = IpsUIPlugin.getImageHandling().createDisabledImageDescriptor(expectedImageDesc);
        assertEquals(expectedImageDesc, imageDesc);
    }

}
