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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;
import org.junit.Test;

public class TestAttributeDecoratorTest extends AbstractIpsPluginTest {

    private final TestAttributeDecorator testAttributeDecorator = new TestAttributeDecorator();

    @Test
    public void testGetDefaultImageDescriptor() {
        ImageDescriptor defaultImageDescriptor = testAttributeDecorator.getDefaultImageDescriptor();

        assertThat(defaultImageDescriptor, is(descriptorOf(TestAttributeDecorator.TEST_ATTRIBUTE_IMAGE)));
        assertThat(defaultImageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_Null() {
        ImageDescriptor imageDescriptor = testAttributeDecorator.getImageDescriptor(null);

        assertThat(imageDescriptor, is(testAttributeDecorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor_NoAttribute() {
        ITestAttribute testAttribute = mock(ITestAttribute.class);

        ImageDescriptor imageDescriptor = testAttributeDecorator.getImageDescriptor(testAttribute);

        assertThat(imageDescriptor, is(testAttributeDecorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor_FindAttributeFails() throws CoreRuntimeException {
        IIpsProject ipsProject = newIpsProject();
        IPolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        IPolicyCmptTypeAttribute attribute = policy.newPolicyCmptTypeAttribute("attr");
        attribute.setChangingOverTime(false);
        attribute.setValueSetConfiguredByProduct(true);
        ITestAttribute testAttribute = mock(ITestAttribute.class);
        when(testAttribute.getIpsProject()).thenReturn(ipsProject);
        doThrow(new CoreRuntimeException("CAN'T FIND IT")).when(testAttribute).findAttribute(ipsProject);

        ImageDescriptor imageDescriptor = testAttributeDecorator.getImageDescriptor(testAttribute);

        assertThat(imageDescriptor, is(testAttributeDecorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor() throws CoreRuntimeException {
        IIpsProject ipsProject = newIpsProject();
        IPolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        IPolicyCmptTypeAttribute attribute = policy.newPolicyCmptTypeAttribute("attr");
        attribute.setChangingOverTime(false);
        attribute.setValueSetConfiguredByProduct(true);
        ITestAttribute testAttribute = mock(ITestAttribute.class);
        when(testAttribute.getIpsProject()).thenReturn(ipsProject);
        when(testAttribute.findAttribute(ipsProject)).thenReturn(attribute);

        ImageDescriptor imageDescriptor = testAttributeDecorator.getImageDescriptor(testAttribute);

        assertThat(imageDescriptor, hasBaseImage(AttributeDecorator.PUBLISHED_BASE_IMAGE));
        assertThat(imageDescriptor, hasOverlay(OverlayIcons.PRODUCT_RELEVANT, IDecoration.TOP_RIGHT));
        assertThat(imageDescriptor, hasOverlay(OverlayIcons.STATIC, IDecoration.TOP_LEFT));
    }

    @Test
    public void testGetLabel() {
        ITestAttribute testAttribute = mock(ITestAttribute.class);
        when(testAttribute.getName()).thenReturn("Foo");

        String label = testAttributeDecorator.getLabel(testAttribute);

        assertThat(label, is("Foo"));
    }

    @Test
    public void testGetLabel_null() {
        assertThat(testAttributeDecorator.getLabel(null), is(""));
    }

}
