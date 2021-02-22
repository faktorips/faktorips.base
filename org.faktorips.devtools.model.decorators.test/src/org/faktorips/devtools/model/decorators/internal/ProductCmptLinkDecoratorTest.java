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
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsElementDecorator;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmptLink;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
public class ProductCmptLinkDecoratorTest extends AbstractIpsPluginTest {

    private IIpsPackageFragmentRoot root;
    private IIpsProject ipsProject;

    private IProductCmpt prodCmpt;

    private IProductCmptGeneration generation;

    private IProductCmptLink link;

    private ProductCmptLinkDecorator decorator;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("AdapterTestProject");
        root = newIpsPackageFragmentRoot(ipsProject, null, "root");
        decorator = new ProductCmptLinkDecorator();
        IProductCmptType prodType = newProductCmptType(root, "ProdCmptType");
        prodCmpt = newProductCmpt(prodType, "ProdCmpt");
        generation = prodCmpt.getProductCmptGeneration(0);
        link = generation.newLink("ProdCmpt");
        link.setTarget("ProdCmpt");
    }

    @Test
    public void testAdapter() {
        IIpsElementDecorator decorator = IIpsDecorators.get(ProductCmptLink.class);
        assertNotNull(decorator);
        assertTrue(decorator instanceof ProductCmptLinkDecorator);
    }

    @Test
    public void testProductCmptIconDesc() {
        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(link);

        assertNotNull(imageDescriptor);
        assertTrue(imageDescriptor instanceof DecorationOverlayIcon);
    }

    @Test
    public void testProductCmptIconDescIsReused() {
        ImageDescriptor imageDescriptor1 = decorator.getImageDescriptor(link);
        IProductCmptLink link2 = generation.newLink("ProdCmpt");
        link2.setTarget("ProdCmpt");
        ImageDescriptor imageDescriptor2 = decorator.getImageDescriptor(link2);

        assertThat(imageDescriptor2, is(sameInstance(imageDescriptor1)));
    }
}
