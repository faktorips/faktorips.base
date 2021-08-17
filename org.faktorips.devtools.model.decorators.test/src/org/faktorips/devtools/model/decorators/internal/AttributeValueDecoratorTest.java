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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.abstracttest.TestIpsModelExtensions;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IDatatypeFormatter;
import org.faktorips.devtools.model.preferences.IIpsModelPreferences;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IValueHolder;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.junit.Test;

public class AttributeValueDecoratorTest {

    private final AttributeValueDecorator attributeValueDecorator = new AttributeValueDecorator();

    @Test
    public void testGetDefaultImageDescriptor() {
        ImageDescriptor defaultImageDescriptor = attributeValueDecorator.getDefaultImageDescriptor();

        assertThat(defaultImageDescriptor, is(descriptorOf(AttributeValueDecorator.PRODUCT_ATTRIBUTE_ICON)));
        assertThat(defaultImageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_Null() {
        assertThat(attributeValueDecorator.getImageDescriptor(null),
                is(attributeValueDecorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor() {
        IAttributeValue attributeValue = mock(IAttributeValue.class);

        ImageDescriptor imageDescriptor = attributeValueDecorator.getImageDescriptor(attributeValue);

        assertThat(imageDescriptor, is(descriptorOf(AttributeValueDecorator.PRODUCT_ATTRIBUTE_ICON)));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetLabel_Null() {
        assertThat(attributeValueDecorator.getLabel(null), is(IpsStringUtils.EMPTY));
    }

    @Test
    public void testGetLabel_NoValueHolder() throws CoreException {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        when(attributeValue.getCaption(any(Locale.class))).thenReturn("Foo");

        assertThat(attributeValueDecorator.getLabel(attributeValue), is("Foo: null"));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testGetLabel_Unformatted() throws CoreException {
        IValueHolder valueHolder = mock(IValueHolder.class);
        when(valueHolder.getStringValue()).thenReturn("Bar");

        IAttributeValue attributeValue = mock(IAttributeValue.class);
        when(attributeValue.getCaption(any(Locale.class))).thenReturn("Foo");
        when(attributeValue.getValueHolder()).thenReturn(valueHolder);

        String label = attributeValueDecorator.getLabel(attributeValue);

        assertThat(label, is("Foo: Bar"));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testGetLabel_Formatted() throws CoreException {
        IIpsProject ipsProject = mock(IIpsProject.class);

        ValueDatatype datatype = mock(ValueDatatype.class);

        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        when(attribute.findDatatype(ipsProject)).thenReturn(datatype);

        IValueHolder valueHolder = mock(IValueHolder.class);
        when(valueHolder.getStringValue()).thenReturn("Bar");

        IAttributeValue attributeValue = mock(IAttributeValue.class);
        when(attributeValue.getIpsProject()).thenReturn(ipsProject);
        when(attributeValue.getCaption(any(Locale.class))).thenReturn("Foo");
        when(attributeValue.findAttribute(ipsProject)).thenReturn(attribute);
        when(attributeValue.getValueHolder()).thenReturn(valueHolder);

        IDatatypeFormatter datatypeFormatter = mock(IDatatypeFormatter.class);
        when(datatypeFormatter.formatValue(datatype, "Bar")).thenReturn("Baz");

        IIpsModelPreferences modelPreferences = mock(IIpsModelPreferences.class);
        when(modelPreferences.getDatatypeFormatter()).thenReturn(datatypeFormatter);
        try (TestIpsModelExtensions testIpsModelExtensions = TestIpsModelExtensions.using(modelPreferences)) {
            testIpsModelExtensions.setModelPreferences(modelPreferences);

            String label = attributeValueDecorator.getLabel(attributeValue);

            assertThat(label, is("Foo: Baz"));
        }
    }

}
