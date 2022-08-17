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
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.junit.Test;

public class TestPolicyCmptDecoratorTest extends AbstractIpsPluginTest {

    private final TestPolicyCmptDecorator testPolicyCmptDecorator = new TestPolicyCmptDecorator();

    @Test
    public void testGetDefaultImageDescriptor() {
        ImageDescriptor defaultImageDescriptor = testPolicyCmptDecorator.getDefaultImageDescriptor();

        assertThat(defaultImageDescriptor, is(descriptorOf(TestPolicyCmptDecorator.POLICY_CMPT_INSTANCE_IMAGE)));
        assertThat(defaultImageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_Null() {
        ImageDescriptor imageDescriptor = testPolicyCmptDecorator.getImageDescriptor(null);

        assertThat(imageDescriptor, is(testPolicyCmptDecorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor() {
        ITestPolicyCmpt testPolicyCmpt = mock(ITestPolicyCmpt.class);

        ImageDescriptor imageDescriptor = testPolicyCmptDecorator.getImageDescriptor(testPolicyCmpt);

        assertThat(imageDescriptor, is(descriptorOf(TestPolicyCmptDecorator.POLICY_CMPT_INSTANCE_IMAGE)));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_ProductRelevant() {
        ITestPolicyCmpt testPolicyCmpt = mock(ITestPolicyCmpt.class);
        when(testPolicyCmpt.isProductRelevant()).thenReturn(true);

        ImageDescriptor imageDescriptor = testPolicyCmptDecorator.getImageDescriptor(testPolicyCmpt);

        assertThat(imageDescriptor, hasBaseImage(TestPolicyCmptDecorator.POLICY_CMPT_INSTANCE_IMAGE));
        assertThat(imageDescriptor, hasOverlay(OverlayIcons.PRODUCT_RELEVANT, IDecoration.TOP_RIGHT));
    }

    @Test
    public void testGetLabel() {
        ITestPolicyCmpt testPolicyCmpt = mock(ITestPolicyCmpt.class);
        when(testPolicyCmpt.getName()).thenReturn("Foo");

        String label = testPolicyCmptDecorator.getLabel(testPolicyCmpt);

        assertThat(label, is("Foo"));
    }

    @Test
    public void testGetLabel_Null() {
        assertThat(testPolicyCmptDecorator.getLabel(null), is(IpsStringUtils.EMPTY));
    }
}
