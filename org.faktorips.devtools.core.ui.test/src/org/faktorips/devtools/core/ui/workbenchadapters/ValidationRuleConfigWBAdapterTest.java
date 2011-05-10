/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import junit.framework.Assert;

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
        Assert.assertEquals(expectedImageDesc, imageDesc);
    }

    @Test
    public void testGetInactiveImage() {
        when(config.isActive()).thenReturn(false);

        ImageDescriptor imageDesc = adapter.getImageDescriptor(config);

        ImageDescriptor expectedImageDesc = IpsUIPlugin.getImageHandling().getSharedImageDescriptor(
                "ValidationRuleDef.gif", true);
        expectedImageDesc = IpsUIPlugin.getImageHandling().createDisabledImageDescriptor(expectedImageDesc);
        Assert.assertEquals(expectedImageDesc, imageDesc);
    }

}
