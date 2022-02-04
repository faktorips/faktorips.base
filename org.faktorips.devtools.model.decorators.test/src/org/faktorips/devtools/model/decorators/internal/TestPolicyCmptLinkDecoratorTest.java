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

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.junit.Test;

public class TestPolicyCmptLinkDecoratorTest extends AbstractIpsPluginTest {

    private final TestPolicyCmptLinkDecorator testPolicyCmptLinkDecorator = new TestPolicyCmptLinkDecorator();

    @Test
    public void testGetDefaultImageDescriptor() {
        ImageDescriptor defaultImageDescriptor = testPolicyCmptLinkDecorator.getDefaultImageDescriptor();

        assertThat(defaultImageDescriptor, is(descriptorOf(AssociationDecorator.ASSOCIATION_TYPE_COMPOSITION_IMAGE)));
    }

    @Test
    public void testGetImageDescriptor_Null() {
        ImageDescriptor imageDescriptor = testPolicyCmptLinkDecorator.getImageDescriptor(null);

        assertThat(imageDescriptor, is(testPolicyCmptLinkDecorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor_AssociationNoTarget() {
        ITestPolicyCmptLink testPolicyCmptLink = mock(ITestPolicyCmptLink.class);
        when(testPolicyCmptLink.isAssociation()).thenReturn(true);

        ImageDescriptor imageDescriptor = testPolicyCmptLinkDecorator.getImageDescriptor(testPolicyCmptLink);

        assertThat(imageDescriptor, is(descriptorOf(TestPolicyCmptLinkDecorator.LINKED_POLICY_CMPT_TYPE_IMAGE)));
    }

    @Test
    public void testGetImageDescriptor_Association() {
        ITestPolicyCmptLink testPolicyCmptLink = mock(ITestPolicyCmptLink.class);
        when(testPolicyCmptLink.isAssociation()).thenReturn(true);
        ITestPolicyCmpt targetPolicyCmpt = mock(ITestPolicyCmpt.class);
        when(testPolicyCmptLink.findTarget()).thenReturn(targetPolicyCmpt);

        ImageDescriptor imageDescriptor = testPolicyCmptLinkDecorator.getImageDescriptor(testPolicyCmptLink);

        assertThat(imageDescriptor, is(descriptorOf(TestPolicyCmptLinkDecorator.LINKED_POLICY_CMPT_TYPE_IMAGE)));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_AssociationTargetWithProduct() {
        ITestPolicyCmptLink testPolicyCmptLink = mock(ITestPolicyCmptLink.class);
        when(testPolicyCmptLink.isAssociation()).thenReturn(true);
        ITestPolicyCmpt targetPolicyCmpt = mock(ITestPolicyCmpt.class);
        when(testPolicyCmptLink.findTarget()).thenReturn(targetPolicyCmpt);
        when(targetPolicyCmpt.hasProductCmpt()).thenReturn(true);

        ImageDescriptor imageDescriptor = testPolicyCmptLinkDecorator.getImageDescriptor(testPolicyCmptLink);

        assertThat(imageDescriptor, is(descriptorOf(TestPolicyCmptLinkDecorator.LINK_PRODUCT_CMPT_IMAGE)));
    }

    @Test
    public void testGetImageDescriptor_FindTestPolicyCmptTypeParameterFails() throws CoreRuntimeException {
        IIpsProject ipsProject = newIpsProject();
        IPolicyCmptType parentPolicyCmptType = newPolicyCmptType(ipsProject, "Parent");
        IPolicyCmptTypeAssociation association = parentPolicyCmptType.newPolicyCmptTypeAssociation();
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        ITestPolicyCmptLink testPolicyCmptLink = mock(ITestPolicyCmptLink.class);
        when(testPolicyCmptLink.isAssociation()).thenReturn(false);
        when(testPolicyCmptLink.getIpsProject()).thenReturn(ipsProject);
        doThrow(new CoreRuntimeException(new IpsStatus("CAN'T FIND IT"))).when(testPolicyCmptLink)
                .findTestPolicyCmptTypeParameter(ipsProject);

        ImageDescriptor imageDescriptor = testPolicyCmptLinkDecorator.getImageDescriptor(testPolicyCmptLink);

        assertThat(imageDescriptor, is(testPolicyCmptLinkDecorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor_Composition() throws CoreRuntimeException {
        IIpsProject ipsProject = newIpsProject();
        IPolicyCmptType parentPolicyCmptType = newPolicyCmptType(ipsProject, "Parent");
        IPolicyCmptTypeAssociation association = parentPolicyCmptType.newPolicyCmptTypeAssociation();
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        ITestPolicyCmptLink testPolicyCmptLink = mock(ITestPolicyCmptLink.class);
        when(testPolicyCmptLink.isAssociation()).thenReturn(false);
        when(testPolicyCmptLink.getIpsProject()).thenReturn(ipsProject);
        ITestPolicyCmptTypeParameter parameter = mock(ITestPolicyCmptTypeParameter.class);
        when(testPolicyCmptLink.findTestPolicyCmptTypeParameter(ipsProject)).thenReturn(parameter);
        when(parameter.findAssociation(ipsProject)).thenReturn(association);

        ImageDescriptor imageDescriptor = testPolicyCmptLinkDecorator.getImageDescriptor(testPolicyCmptLink);

        assertThat(imageDescriptor,
                hasBaseImage(AssociationDecorator.ASSOCIATION_TYPE_COMPOSITION_DETAIL_TO_MASTER_IMAGE));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_CompositionAssociationNotFound() throws CoreRuntimeException {
        IIpsProject ipsProject = mock(IIpsProject.class);
        ITestPolicyCmptLink testPolicyCmptLink = mock(ITestPolicyCmptLink.class);
        when(testPolicyCmptLink.isAssociation()).thenReturn(false);
        when(testPolicyCmptLink.getIpsProject()).thenReturn(ipsProject);
        ITestPolicyCmptTypeParameter parameter = mock(ITestPolicyCmptTypeParameter.class);
        when(testPolicyCmptLink.findTestPolicyCmptTypeParameter(ipsProject)).thenReturn(parameter);

        ImageDescriptor imageDescriptor = testPolicyCmptLinkDecorator.getImageDescriptor(testPolicyCmptLink);

        assertThat(imageDescriptor, is(descriptorOf(AssociationDecorator.ASSOCIATION_TYPE_COMPOSITION_IMAGE)));
    }

    @Test
    public void testGetImageDescriptor_CompositionParameterNotFound() {
        ITestPolicyCmptLink testPolicyCmptLink = mock(ITestPolicyCmptLink.class);
        when(testPolicyCmptLink.isAssociation()).thenReturn(false);

        ImageDescriptor imageDescriptor = testPolicyCmptLinkDecorator.getImageDescriptor(testPolicyCmptLink);

        assertThat(imageDescriptor, is(descriptorOf(AssociationDecorator.ASSOCIATION_TYPE_COMPOSITION_IMAGE)));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetLabel() {
        ITestPolicyCmptLink testPolicyCmptLink = mock(ITestPolicyCmptLink.class);
        when(testPolicyCmptLink.getName()).thenReturn("foo");

        String label = testPolicyCmptLinkDecorator.getLabel(testPolicyCmptLink);

        assertThat(label, is("foo"));
    }

    @Test
    public void testGetLabel_Null() {
        assertThat(testPolicyCmptLinkDecorator.getLabel(null), is(IpsStringUtils.EMPTY));
    }
}
