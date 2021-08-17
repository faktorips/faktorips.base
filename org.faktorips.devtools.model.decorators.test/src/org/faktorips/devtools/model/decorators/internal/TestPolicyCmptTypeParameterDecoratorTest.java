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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.junit.Test;

public class TestPolicyCmptTypeParameterDecoratorTest extends AbstractIpsPluginTest {

    private final TestPolicyCmptTypeParameterDecorator testPolicyCmptTypeParameterDecorator = new TestPolicyCmptTypeParameterDecorator();

    @Test
    public void testGetDefaultImageDescriptor() {
        ImageDescriptor defaultImageDescriptor = testPolicyCmptTypeParameterDecorator.getDefaultImageDescriptor();

        assertThat(defaultImageDescriptor, is(descriptorOf(AssociationDecorator.ASSOCIATION_TYPE_COMPOSITION_IMAGE)));
        assertThat(defaultImageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_Null() {
        ImageDescriptor imageDescriptor = testPolicyCmptTypeParameterDecorator.getImageDescriptor(null);

        assertThat(imageDescriptor, is(testPolicyCmptTypeParameterDecorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor_RootWithAssociation() {
        ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter = mock(ITestPolicyCmptTypeParameter.class);
        when(testPolicyCmptTypeParameter.getAssociation()).thenReturn("pol");
        when(testPolicyCmptTypeParameter.isRoot()).thenReturn(true);

        ImageDescriptor imageDescriptor = testPolicyCmptTypeParameterDecorator
                .getImageDescriptor(testPolicyCmptTypeParameter);

        assertThat(imageDescriptor, is(testPolicyCmptTypeParameterDecorator.getDefaultImageDescriptor()));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_RootPolicy() {
        ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter = mock(ITestPolicyCmptTypeParameter.class);

        ImageDescriptor imageDescriptor = testPolicyCmptTypeParameterDecorator
                .getImageDescriptor(testPolicyCmptTypeParameter);

        assertThat(imageDescriptor, is(descriptorOf(IpsDecorators.POLICY_CMPT_TYPE_IMAGE)));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_RootConfiguredPolicy() {
        ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter = mock(ITestPolicyCmptTypeParameter.class);
        when(testPolicyCmptTypeParameter.isRequiresProductCmpt()).thenReturn(true);

        ImageDescriptor imageDescriptor = testPolicyCmptTypeParameterDecorator
                .getImageDescriptor(testPolicyCmptTypeParameter);

        assertThat(imageDescriptor, is(descriptorOf(IpsDecorators.PRODUCT_CMPT_TYPE_IMAGE)));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_AssociationNotFound() {
        ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter = mock(ITestPolicyCmptTypeParameter.class);
        when(testPolicyCmptTypeParameter.getAssociation()).thenReturn("pol");

        ImageDescriptor imageDescriptor = testPolicyCmptTypeParameterDecorator
                .getImageDescriptor(testPolicyCmptTypeParameter);

        assertThat(imageDescriptor, is(testPolicyCmptTypeParameterDecorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor_FindAssociationFails() throws CoreException {
        IIpsProject ipsProject = newIpsProject();
        IPolicyCmptType parentPolicyCmptType = newPolicyCmptType(ipsProject, "Parent");
        IPolicyCmptTypeAssociation association = parentPolicyCmptType.newPolicyCmptTypeAssociation();
        association.setAssociationType(AssociationType.AGGREGATION);
        ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter = mock(ITestPolicyCmptTypeParameter.class);
        when(testPolicyCmptTypeParameter.getIpsProject()).thenReturn(ipsProject);
        when(testPolicyCmptTypeParameter.getAssociation()).thenReturn("agg");
        doThrow(new CoreException(new IpsStatus("CAN'T FIND IT"))).when(testPolicyCmptTypeParameter)
                .findAssociation(ipsProject);

        ImageDescriptor imageDescriptor = testPolicyCmptTypeParameterDecorator
                .getImageDescriptor(testPolicyCmptTypeParameter);

        assertThat(imageDescriptor, is(testPolicyCmptTypeParameterDecorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor_Association() throws CoreException {
        IIpsProject ipsProject = newIpsProject();
        IPolicyCmptType parentPolicyCmptType = newPolicyCmptType(ipsProject, "Parent");
        IPolicyCmptTypeAssociation association = parentPolicyCmptType.newPolicyCmptTypeAssociation();
        association.setAssociationType(AssociationType.AGGREGATION);
        ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter = mock(ITestPolicyCmptTypeParameter.class);
        when(testPolicyCmptTypeParameter.getIpsProject()).thenReturn(ipsProject);
        when(testPolicyCmptTypeParameter.getAssociation()).thenReturn("agg");
        when(testPolicyCmptTypeParameter.findAssociation(ipsProject)).thenReturn(association);

        ImageDescriptor imageDescriptor = testPolicyCmptTypeParameterDecorator
                .getImageDescriptor(testPolicyCmptTypeParameter);

        assertThat(imageDescriptor, hasBaseImage(AssociationDecorator.ASSOCIATION_TYPE_AGGREGATION_IMAGE));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetLabel() {
        ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter = mock(ITestPolicyCmptTypeParameter.class);
        when(testPolicyCmptTypeParameter.getName()).thenReturn("Foo");

        String label = testPolicyCmptTypeParameterDecorator.getLabel(testPolicyCmptTypeParameter);

        assertThat(label, is("Foo"));
    }

    @Test
    public void testGetLabel_Null() {
        assertThat(testPolicyCmptTypeParameterDecorator.getLabel(null), is(IpsStringUtils.EMPTY));
    }
}
