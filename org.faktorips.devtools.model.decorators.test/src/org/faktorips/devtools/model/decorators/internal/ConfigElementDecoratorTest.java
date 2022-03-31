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

import static org.faktorips.devtools.model.decorators.internal.ImageDescriptorMatchers.descriptorOf;
import static org.faktorips.devtools.model.decorators.internal.ImageDescriptorMatchers.hasNoOverlay;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.abstracttest.TestIpsModelExtensions;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IDatatypeFormatter;
import org.faktorips.devtools.model.preferences.IIpsModelPreferences;
import org.faktorips.devtools.model.productcmpt.IConfigElement;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.junit.Test;

public class ConfigElementDecoratorTest {

    private final ConfigElementDecorator configElementDecorator = new ConfigElementDecorator();

    @Test
    public void testGetDefaultImageDescriptor() {
        ImageDescriptor defaultImageDescriptor = configElementDecorator.getDefaultImageDescriptor();

        assertThat(defaultImageDescriptor, is(descriptorOf(ConfigElementDecorator.CONFIG_ELEMENT_ICON)));
        assertThat(defaultImageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_Null() {
        assertThat(configElementDecorator.getImageDescriptor(null),
                is(configElementDecorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor_ConfiguredDefault() {
        IConfigElement configElement = mock(IConfiguredDefault.class);

        ImageDescriptor imageDescriptor = configElementDecorator.getImageDescriptor(configElement);

        assertThat(imageDescriptor, is(descriptorOf(ConfigElementDecorator.CONFIGURED_DEFAULT_ICON)));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_ConfiguredValueSet() {
        IConfigElement configElement = mock(IConfiguredValueSet.class);

        ImageDescriptor imageDescriptor = configElementDecorator.getImageDescriptor(configElement);

        assertThat(imageDescriptor, is(descriptorOf(ConfigElementDecorator.CONFIGURED_VALUE_SET_ICON)));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetLabel_Null() {
        assertThat(configElementDecorator.getLabel(null), is(IpsStringUtils.EMPTY));
    }

    @Test
    public void testGetLabel_NonStringValue() {
        IConfigElement configElement = mock(IConfigElement.class);
        when(configElement.getCaption(any(Locale.class))).thenReturn("Foo");
        when(configElement.getPropertyValue()).thenReturn(4711);

        String label = configElementDecorator.getLabel(configElement);

        assertThat(label, is("Foo"));
    }

    @Test
    public void testGetLabel_Unformatted() {
        IConfigElement configElement = mock(IConfigElement.class);
        when(configElement.getCaption(any(Locale.class))).thenReturn("Foo");
        when(configElement.getPropertyValue()).thenReturn("Bar");

        String label = configElementDecorator.getLabel(configElement);

        assertThat(label, is("Foo: Bar"));
    }

    @Test
    public void testGetLabel_Formatted() {
        IIpsProject ipsProject = mock(IIpsProject.class);

        ValueDatatype datatype = mock(ValueDatatype.class);

        IConfigElement configElement = mock(IConfigElement.class);
        when(configElement.getIpsProject()).thenReturn(ipsProject);
        when(configElement.getCaption(any(Locale.class))).thenReturn("Foo");
        when(configElement.getPropertyValue()).thenReturn("Bar");
        when(configElement.findValueDatatype(ipsProject)).thenReturn(datatype);

        IDatatypeFormatter datatypeFormatter = mock(IDatatypeFormatter.class);
        when(datatypeFormatter.formatValue(datatype, "Bar")).thenReturn("Baz");

        IIpsModelPreferences modelPreferences = mock(IIpsModelPreferences.class);
        when(modelPreferences.getDatatypeFormatter()).thenReturn(datatypeFormatter);
        try (TestIpsModelExtensions testIpsModelExtensions = TestIpsModelExtensions.using(modelPreferences)) {

            String label = configElementDecorator.getLabel(configElement);

            assertThat(label, is("Foo: Baz"));
        }
    }

}
