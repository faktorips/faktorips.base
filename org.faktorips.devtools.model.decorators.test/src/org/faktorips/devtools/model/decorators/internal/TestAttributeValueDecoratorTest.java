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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.junit.Test;

public class TestAttributeValueDecoratorTest extends AbstractIpsPluginTest {

    private final TestAttributeValueDecorator testAttributeValueDecorator = new TestAttributeValueDecorator();

    @Test
    public void testGetDefaultImageDescriptor() {
        ImageDescriptor defaultImageDescriptor = testAttributeValueDecorator.getDefaultImageDescriptor();

        assertThat(defaultImageDescriptor, is(descriptorOf(TestAttributeValueDecorator.TEST_ATTRIBUTE_VALUE_IMAGE)));
        assertThat(defaultImageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_Null() {
        ImageDescriptor imageDescriptor = testAttributeValueDecorator.getImageDescriptor(null);

        assertThat(imageDescriptor, is(testAttributeValueDecorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor_NoAttribute() {
        ITestAttributeValue testAttributeValue = mock(ITestAttributeValue.class);

        ImageDescriptor imageDescriptor = testAttributeValueDecorator.getImageDescriptor(testAttributeValue);

        assertThat(imageDescriptor, is(testAttributeValueDecorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor_FindTestAttributeFails() throws CoreRuntimeException {
        IIpsProject ipsProject = mock(IIpsProject.class);
        ITestAttributeValue testAttributeValue = mock(ITestAttributeValue.class);
        when(testAttributeValue.getIpsProject()).thenReturn(ipsProject);
        doThrow(new CoreException(new IpsStatus("CAN'T FIND IT"))).when(testAttributeValue)
                .findTestAttribute(ipsProject);

        ImageDescriptor imageDescriptor = testAttributeValueDecorator.getImageDescriptor(testAttributeValue);

        assertThat(imageDescriptor, is(testAttributeValueDecorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor() throws CoreRuntimeException {
        IIpsProject ipsProject = newIpsProject();
        IPolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        IPolicyCmptTypeAttribute attribute = policy.newPolicyCmptTypeAttribute("attr");
        attribute.setChangingOverTime(false);
        attribute.setValueSetConfiguredByProduct(true);
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "Test");
        ITestPolicyCmptTypeParameter parameter = testCaseType.newInputTestPolicyCmptTypeParameter();
        ITestAttribute testAttribute = parameter.newInputTestAttribute();
        testAttribute.setAttribute(attribute);
        ITestAttributeValue testAttributeValue = mock(ITestAttributeValue.class);
        when(testAttributeValue.getIpsProject()).thenReturn(ipsProject);
        when(testAttributeValue.findTestAttribute(ipsProject)).thenReturn(testAttribute);

        ImageDescriptor imageDescriptor = testAttributeValueDecorator.getImageDescriptor(testAttributeValue);

        assertThat(imageDescriptor, hasBaseImage(AttributeDecorator.PUBLISHED_BASE_IMAGE));
        assertThat(imageDescriptor, hasOverlay(OverlayIcons.PRODUCT_RELEVANT, IDecoration.TOP_RIGHT));
        assertThat(imageDescriptor, hasOverlay(OverlayIcons.STATIC, IDecoration.TOP_LEFT));
    }

    @Test
    public void testGetLabel() {
        ITestAttributeValue testAttributeValue = mock(ITestAttributeValue.class);
        when(testAttributeValue.getName()).thenReturn("Foo");

        String label = testAttributeValueDecorator.getLabel(testAttributeValue);

        assertThat(label, is("Foo"));
    }

    @Test
    public void testGetLabel_null() {
        assertThat(testAttributeValueDecorator.getLabel(null), is(""));
    }

}
