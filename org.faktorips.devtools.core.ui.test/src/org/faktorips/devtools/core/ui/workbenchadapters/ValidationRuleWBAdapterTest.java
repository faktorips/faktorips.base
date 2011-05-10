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

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import junit.framework.Assert;

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
        Assert.assertEquals(expectedImageDesc, imageDesc);
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
