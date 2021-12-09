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
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.productcmpt.IValidationRuleConfig;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Stefan Widmaier
 */
public class ValidationRuleConfigDecoratorTest extends AbstractIpsPluginTest {

    private IValidationRuleConfig config;
    private ValidationRuleConfigDecorator decorator;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        config = mock(IValidationRuleConfig.class);
        when(config.getName()).thenReturn("RegelName");
        when(config.isActive()).thenReturn(true);
        decorator = new ValidationRuleConfigDecorator();
    }

    @Test
    public void testGetImageDescriptor_Null() {
        assertThat(decorator.getImageDescriptor(null), is(decorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor_Active() {
        ImageDescriptor imageDesc = decorator.getImageDescriptor(config);

        ImageDescriptor expectedImageDesc = IIpsDecorators.getImageHandling().getSharedImageDescriptor(
                "ValidationRuleDef.gif", true);
        assertEquals(expectedImageDesc, imageDesc);
    }

    @Test
    public void testGetImageDescriptor_Inactive() {
        when(config.isActive()).thenReturn(false);

        ImageDescriptor imageDesc = decorator.getImageDescriptor(config);

        ImageDescriptor expectedImageDesc = IIpsDecorators.getImageHandling().getSharedImageDescriptor(
                "ValidationRuleDef.gif", true);
        expectedImageDesc = IIpsDecorators.getImageHandling().getDisabledImageDescriptor(expectedImageDesc);
        assertEquals(expectedImageDesc, imageDesc);
    }

    @Test
    public void testGetLabel() throws CoreRuntimeException {
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(config.getIpsProject()).thenReturn(ipsProject);
        IIpsProjectProperties properties = mock(IIpsProjectProperties.class);
        when(ipsProject.getReadOnlyProperties()).thenReturn(properties);
        ISupportedLanguage language = mock(ISupportedLanguage.class);
        when(properties.getDefaultLanguage()).thenReturn(language);
        when(language.getLocale()).thenReturn(Locale.CANADA_FRENCH);
        when(config.getCaption(Locale.CANADA_FRENCH)).thenReturn("Foo");

        assertEquals("Foo", decorator.getLabel(config));
    }

    @Test
    public void testGetLabel_Null() {
        assertEquals("", decorator.getLabel(null));
    }

}
