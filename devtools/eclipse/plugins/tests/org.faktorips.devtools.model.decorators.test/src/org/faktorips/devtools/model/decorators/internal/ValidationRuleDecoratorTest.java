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

import static org.faktorips.devtools.model.decorators.internal.ImageDescriptorMatchers.hasBaseImage;
import static org.faktorips.devtools.model.decorators.internal.ImageDescriptorMatchers.hasOverlay;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.ipsobject.ILabel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.junit.Before;
import org.junit.Test;

public class ValidationRuleDecoratorTest extends AbstractIpsPluginTest {

    private IValidationRule rule;
    private ValidationRuleDecorator adapter;
    private String[] overlays = new String[4];

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        rule = mock(IValidationRule.class);
        when(rule.getName()).thenReturn("RegelName");
        when(rule.isConfigurableByProductComponent()).thenReturn(false);

        adapter = new ValidationRuleDecorator();
    }

    @Test
    public void testGetImage() {
        ImageDescriptor imageDesc = adapter.getImageDescriptor(rule);
        ImageDescriptor expectedImageDesc = IIpsDecorators.getImageHandling()
                .getSharedImageDescriptor(ValidationRuleDecorator.VALIDATION_RULE_DEF_BASE_IMAGE, true);
        assertEquals(expectedImageDesc, imageDesc);
    }

    @Test
    public void testGetImage_Deprecated() {
        when(rule.isDeprecated()).thenReturn(true);

        ImageDescriptor imageDesc = adapter.getImageDescriptor(rule);

        assertThat(imageDesc, hasBaseImage(ValidationRuleDecorator.VALIDATION_RULE_DEF_BASE_IMAGE));
        assertThat(imageDesc, hasOverlay(OverlayIcons.DEPRECATED, IDecoration.BOTTOM_LEFT));
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
    public void testGetLabel() {
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(rule.getIpsProject()).thenReturn(ipsProject);
        IIpsProjectProperties properties = mock(IIpsProjectProperties.class);
        when(ipsProject.getReadOnlyProperties()).thenReturn(properties);
        ISupportedLanguage language = mock(ISupportedLanguage.class);
        when(properties.getDefaultLanguage()).thenReturn(language);
        when(language.getLocale()).thenReturn(Locale.ITALY);
        ILabel label = mock(ILabel.class);
        when(rule.getLabel(Locale.ITALY)).thenReturn(label);
        when(label.getValue()).thenReturn("RegelName");

        assertEquals("RegelName", adapter.getLabel(rule));
    }

    private ImageDescriptor createImageDescriptorWithOverlays(boolean productRelevant, boolean nonChangingOverTime) {
        if (productRelevant) {
            overlays[1] = OverlayIcons.PRODUCT_RELEVANT;
            if (!nonChangingOverTime) {
                overlays[0] = OverlayIcons.STATIC;
            }
        }
        return IIpsDecorators.getImageHandling()
                .getSharedOverlayImageDescriptor(ValidationRuleDecorator.VALIDATION_RULE_DEF_BASE_IMAGE, overlays);
    }

}
