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
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsElementDecorator;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmptLink;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.junit.Before;
import org.junit.Test;

public class ProductCmptLinkDecoratorTest extends AbstractIpsPluginTest {

    private IIpsPackageFragmentRoot root;
    private IIpsProject ipsProject;

    private IProductCmpt prodCmpt;

    private IProductCmptGeneration generation;

    private IProductCmptLink link;

    private ProductCmptLinkDecorator decorator;
    private IProductCmptType prodType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("AdapterTestProject");
        root = newIpsPackageFragmentRoot(ipsProject, null, "root");
        decorator = new ProductCmptLinkDecorator();
        prodType = newProductCmptType(root, "ProdCmptType");
        prodCmpt = newProductCmpt(prodType, "ProdCmpt");
        generation = prodCmpt.getProductCmptGeneration(0);
        link = generation.newLink("prod");
        link.setTarget("ProdCmpt");
    }

    @Test
    public void testAdapter() {
        IIpsElementDecorator decorator = IIpsDecorators.get(ProductCmptLink.class);
        assertNotNull(decorator);
        assertTrue(decorator instanceof ProductCmptLinkDecorator);
    }

    @Test
    public void testGetDefaultImageDescriptor() {
        ImageDescriptor defaultImageDescriptor = decorator.getDefaultImageDescriptor();

        assertThat(defaultImageDescriptor, is(descriptorOf(ProductCmptLinkDecorator.PRODUCT_CMPT_LINK_IMAGE)));
        assertThat(defaultImageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_Null() {
        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(null);

        assertThat(imageDescriptor, is(decorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor_CoreExceptionWhenFindigTarget() throws CoreException {
        link = mock(IProductCmptLink.class);
        doThrow(new CoreException(new IpsStatus("CAN'T FIND IT"))).when(link).findTarget(any(IIpsProject.class));

        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(link);

        assertThat(imageDescriptor, is(decorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor() {
        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(link);

        assertThat(imageDescriptor, hasBaseImage(ProductCmptDecorator.PRODUCT_CMPT_BASE_IMAGE));
        assertThat(imageDescriptor, hasOverlay(OverlayIcons.LINK, IDecoration.BOTTOM_RIGHT));
    }

    @Test
    public void testGetImageDescriptor_CustomProductCmptTypeIcon() throws CoreException, IOException {
        IFile file = ipsProject.getProject().getFile("/root/foo.gif");
        file.create(IpsModelDecoratorsPluginActivator.getBundle().getEntry("icons/TestCase.gif").openStream(), true,
                null);
        prodType.setInstancesIcon("foo.gif");

        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(link);

        assertThat(imageDescriptor, hasBaseImage("TestCase.gif"));
        assertThat(imageDescriptor, hasOverlay(OverlayIcons.LINK, IDecoration.BOTTOM_RIGHT));
    }

    @Test
    public void testProductCmptIconDescIsReused() {
        ImageDescriptor imageDescriptor1 = decorator.getImageDescriptor(link);
        IProductCmptLink link2 = generation.newLink("ProdCmpt");
        link2.setTarget("ProdCmpt");
        ImageDescriptor imageDescriptor2 = decorator.getImageDescriptor(link2);

        assertThat(imageDescriptor2, is(sameInstance(imageDescriptor1)));
    }

    @Test
    public void testGetLabel() {
        assertThat(decorator.getLabel(link), is("ProdCmpt"));
    }

    @Test
    public void testGetLabel_Null() {
        assertThat(decorator.getLabel(null), is(IpsStringUtils.EMPTY));
    }

    @Test
    public void testGetLabel_TargetNotFound() {
        link.setTarget("DOES NOT EXIST");

        String label = decorator.getLabel(link);

        assertThat(label, is(IpsStringUtils.EMPTY));
    }

    @Test
    public void testGetLabel_CoreExceptionWhenFindigTarget() throws CoreException {
        link = mock(IProductCmptLink.class);
        when(link.getName()).thenReturn("foo");
        doThrow(new CoreException(new IpsStatus("CAN'T FIND IT"))).when(link).findTarget(any(IIpsProject.class));

        String label = decorator.getLabel(link);

        assertThat(label, is("foo"));
    }
}
