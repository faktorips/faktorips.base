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
import static org.faktorips.devtools.model.decorators.internal.ImageDescriptorMatchers.hasBaseImage;
import static org.faktorips.devtools.model.decorators.internal.ImageDescriptorMatchers.hasNoOverlay;
import static org.faktorips.devtools.model.decorators.internal.ImageDescriptorMatchers.hasOverlay;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.ipsobject.Modifier;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.type.IAttribute;
import org.junit.Test;

public class AttributeDecoratorTest {

    private final AttributeDecorator attributeDecorator = new AttributeDecorator();

    @Test
    public void testGetDefaultImageDescriptor() {
        ImageDescriptor defaultImageDescriptor = attributeDecorator.getDefaultImageDescriptor();

        assertThat(defaultImageDescriptor, is(descriptorOf(AttributeDecorator.PUBLISHED_BASE_IMAGE)));
        assertThat(defaultImageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_Null() {
        ImageDescriptor imageDescriptor = attributeDecorator.getImageDescriptor(null);

        assertThat(imageDescriptor, is(descriptorOf(AttributeDecorator.PUBLISHED_BASE_IMAGE)));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_Overwrite() {
        IAttribute overwritingAttribute = mock(IAttribute.class);
        when(overwritingAttribute.getModifier()).thenReturn(Modifier.PUBLISHED);
        when(overwritingAttribute.isOverwrite()).thenReturn(true);

        ImageDescriptor imageDescriptor = attributeDecorator.getImageDescriptor(overwritingAttribute);

        assertThat(imageDescriptor, hasBaseImage(AttributeDecorator.PUBLISHED_BASE_IMAGE));
        assertThat(imageDescriptor, hasOverlay(OverlayIcons.OVERRIDE, IDecoration.BOTTOM_RIGHT));
    }

    @Test
    public void testGetImageDescriptor_Deprecated() {
        IAttribute overwritingAttribute = mock(IAttribute.class);
        when(overwritingAttribute.getModifier()).thenReturn(Modifier.PUBLISHED);
        when(overwritingAttribute.isDeprecated()).thenReturn(true);

        ImageDescriptor imageDescriptor = attributeDecorator.getImageDescriptor(overwritingAttribute);

        assertThat(imageDescriptor, hasBaseImage(AttributeDecorator.PUBLISHED_BASE_IMAGE));
        assertThat(imageDescriptor, hasOverlay(OverlayIcons.DEPRECATED, IDecoration.BOTTOM_LEFT));
    }

    @Test
    public void testGetImageDescriptor_Published() {
        IAttribute publishedAttribute = mock(IAttribute.class);
        when(publishedAttribute.getModifier()).thenReturn(Modifier.PUBLISHED);

        ImageDescriptor imageDescriptor = attributeDecorator.getImageDescriptor(publishedAttribute);

        assertThat(imageDescriptor, hasBaseImage(AttributeDecorator.PUBLISHED_BASE_IMAGE));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_Public() {
        IAttribute publicAttribute = mock(IAttribute.class);
        when(publicAttribute.getModifier()).thenReturn(Modifier.PUBLIC);

        ImageDescriptor imageDescriptor = attributeDecorator.getImageDescriptor(publicAttribute);

        assertThat(imageDescriptor, hasBaseImage(AttributeDecorator.PUBLIC_BASE_IMAGE));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_DynamicProduct() {
        IProductCmptTypeAttribute dynamicProductAttribute = mock(IProductCmptTypeAttribute.class);
        when(dynamicProductAttribute.getModifier()).thenReturn(Modifier.PUBLISHED);
        when(dynamicProductAttribute.isChangingOverTime()).thenReturn(true);

        ImageDescriptor imageDescriptor = attributeDecorator.getImageDescriptor(dynamicProductAttribute);

        assertThat(imageDescriptor, hasBaseImage(AttributeDecorator.PUBLISHED_BASE_IMAGE));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_NonConfiguredPolicy() {
        IPolicyCmptTypeAttribute nonConfiguredPolicyAttribute = mock(IPolicyCmptTypeAttribute.class);
        when(nonConfiguredPolicyAttribute.getModifier()).thenReturn(Modifier.PUBLISHED);
        when(nonConfiguredPolicyAttribute.isProductRelevant()).thenReturn(false);
        when(nonConfiguredPolicyAttribute.isChangingOverTime()).thenReturn(true);

        ImageDescriptor imageDescriptor = attributeDecorator.getImageDescriptor(nonConfiguredPolicyAttribute);

        assertThat(imageDescriptor, hasBaseImage(AttributeDecorator.PUBLISHED_BASE_IMAGE));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_DynamicConfiguredPolicy() {
        IPolicyCmptTypeAttribute dynamicConfiguredPolicyAttribute = mock(IPolicyCmptTypeAttribute.class);
        when(dynamicConfiguredPolicyAttribute.getModifier()).thenReturn(Modifier.PUBLISHED);
        when(dynamicConfiguredPolicyAttribute.isProductRelevant()).thenReturn(true);
        when(dynamicConfiguredPolicyAttribute.isChangingOverTime()).thenReturn(true);

        assertThat(attributeDecorator.getImageDescriptor(dynamicConfiguredPolicyAttribute),
                hasBaseImage(AttributeDecorator.PUBLISHED_BASE_IMAGE));
        assertThat(attributeDecorator.getImageDescriptor(dynamicConfiguredPolicyAttribute),
                hasOverlay(OverlayIcons.PRODUCT_RELEVANT, IDecoration.TOP_RIGHT));
    }

    @Test
    public void testGetImageDescriptor_StaticConfiguredPolicy() {
        IPolicyCmptTypeAttribute staticConfiguredPolicyAttribute = mock(IPolicyCmptTypeAttribute.class);
        when(staticConfiguredPolicyAttribute.getModifier()).thenReturn(Modifier.PUBLISHED);
        when(staticConfiguredPolicyAttribute.isProductRelevant()).thenReturn(true);
        when(staticConfiguredPolicyAttribute.isChangingOverTime()).thenReturn(false);

        ImageDescriptor imageDescriptor = attributeDecorator.getImageDescriptor(staticConfiguredPolicyAttribute);

        assertThat(imageDescriptor, hasBaseImage(AttributeDecorator.PUBLISHED_BASE_IMAGE));
        assertThat(imageDescriptor, hasOverlay(OverlayIcons.PRODUCT_RELEVANT, IDecoration.TOP_RIGHT));
        assertThat(imageDescriptor, hasOverlay(OverlayIcons.STATIC, IDecoration.TOP_LEFT));
    }

    @Test
    public void testGetImageDescriptor_StaticProduct() {
        IProductCmptTypeAttribute staticProductAttribute = mock(IProductCmptTypeAttribute.class);
        when(staticProductAttribute.getModifier()).thenReturn(Modifier.PUBLISHED);
        when(staticProductAttribute.isChangingOverTime()).thenReturn(false);

        ImageDescriptor imageDescriptor = attributeDecorator.getImageDescriptor(staticProductAttribute);

        assertThat(imageDescriptor, hasBaseImage(AttributeDecorator.PUBLISHED_BASE_IMAGE));
        assertThat(imageDescriptor, hasOverlay(OverlayIcons.STATIC, IDecoration.TOP_LEFT));
    }

    @Test
    public void testGetLabel() {
        IAttribute attribute = mock(IAttribute.class);
        when(attribute.getName()).thenReturn("Foo");

        String label = attributeDecorator.getLabel(attribute);

        assertThat(label, is("Foo"));
    }

    @Test
    public void testGetLabel_null() {
        assertThat(attributeDecorator.getLabel(null), is(""));
    }

    @Test
    public void testGetLabel_WithDatatype() {
        IAttribute attribute = mock(IAttribute.class);
        when(attribute.getName()).thenReturn("Foo");
        when(attribute.getDatatype()).thenReturn("Bar");

        String label = attributeDecorator.getLabel(attribute);

        assertThat(label, is("Foo : Bar"));
    }

    @Test
    public void testGetLabel_Derived() {
        IAttribute derivedAttribute = mock(IAttribute.class);
        when(derivedAttribute.getName()).thenReturn("Foo");
        when(derivedAttribute.isDerived()).thenReturn(true);

        String label = attributeDecorator.getLabel(derivedAttribute);

        assertThat(label, is("/ Foo"));
    }

    @Test
    public void testGetLabel_DerivedWithDatatype() {
        IAttribute attribute = mock(IAttribute.class);
        when(attribute.getName()).thenReturn("Foo");
        when(attribute.isDerived()).thenReturn(true);
        when(attribute.getDatatype()).thenReturn("Bar");

        String label = attributeDecorator.getLabel(attribute);

        assertThat(label, is("/ Foo : Bar"));
    }

}
