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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsElementDecorator;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.junit.Before;
import org.junit.Test;

public class ProductCmptDecoratorTest extends AbstractIpsPluginTest {

    private IIpsPackageFragmentRoot root;
    private IIpsProject ipsProject;

    private ImageDescriptor prodCmptDefaultIcon;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        prodCmptDefaultIcon = IIpsDecorators.getImageHandling().createImageDescriptor("ProductCmpt.gif");
        ipsProject = newIpsProject("AdapterTestProject");
        root = newIpsPackageFragmentRoot(ipsProject, null, "root");
    }

    @Test
    public void testProductCmptIconDesc() {
        // create Types
        IProductCmptType aSuperType = newProductCmptType(root, "ASuperType");
        IProductCmptType bNormalType = newProductCmptType(root, "BNormalType");
        IProductCmptType cSubType = newProductCmptType(root, "CSubType");
        bNormalType.setSupertype(aSuperType.getQualifiedName());
        cSubType.setSupertype(bNormalType.getQualifiedName());
        // Define Icon
        bNormalType.setInstancesIcon("normalTypeImage.gif");
        // create components
        IProductCmpt aSuperCmpt = newProductCmpt(aSuperType, "SuperProductCmpt");
        IProductCmpt cSubCmpt = newProductCmpt(cSubType, "SubProductCmpt");

        IIpsElementDecorator decorator = IIpsDecorators.get(aSuperCmpt.getClass());
        assertNotNull(decorator);
        assertTrue(decorator instanceof ProductCmptDecorator);

        ProductCmptDecorator cmptDecorator = (ProductCmptDecorator)decorator;

        // A: standard Icons
        ImageDescriptor imageDescriptor = IIpsDecorators.getImageHandling().getImageDescriptor(aSuperCmpt);
        assertEquals(prodCmptDefaultIcon, imageDescriptor);
        IconDesc iconDesc = cmptDecorator.getProductCmptIconDesc(aSuperType);
        assertFalse(iconDesc instanceof PathIconDesc);
        // B: Custom Icon
        var iconDescB = cmptDecorator.getProductCmptIconDesc(bNormalType);
        assertTrue(iconDescB instanceof PathIconDesc);
        assertEquals("normalTypeImage.gif", ((PathIconDesc)iconDescB).getPathToImage());
        // C inherits B's Custom Icon
        var iconDescC = cmptDecorator.getProductCmptIconDesc(cSubType);
        assertTrue(iconDescC instanceof PathIconDesc);
        assertEquals("normalTypeImage.gif", ((PathIconDesc)iconDescC).getPathToImage());
        assertSame(iconDescB, iconDescC);

        cSubType.setInstancesIcon("subTypeImage.gif");
        // C: custom Icon overwrites inherited Icon
        iconDesc = cmptDecorator.getProductCmptIconDesc(cSubType);
        assertTrue(iconDesc instanceof PathIconDesc);
        assertEquals("subTypeImage.gif", ((PathIconDesc)iconDesc).getPathToImage());

        cSubType.setInstancesIcon("");
        // C inherits B's Custom Icons again
        iconDesc = cmptDecorator.getProductCmptIconDesc(cSubType);
        assertTrue(iconDesc instanceof PathIconDesc);
        assertEquals("normalTypeImage.gif", ((PathIconDesc)iconDesc).getPathToImage());

        bNormalType.setInstancesIcon("");
        // C inherits A's standard Icon
        imageDescriptor = IIpsDecorators.getImageHandling().getImageDescriptor(cSubCmpt);
        assertEquals(prodCmptDefaultIcon, imageDescriptor);
        iconDesc = cmptDecorator.getProductCmptIconDesc(cSubType);
        assertFalse(iconDesc instanceof PathIconDesc);
    }

    @Test
    public void testIconDescriptorsForTemplatesAreReused() {
        // create Types
        IProductCmptType productType = newProductCmptType(root, "a.ProductType");
        // create components
        IProductCmpt template = newProductTemplate(productType, "a.Template");
        IProductCmpt standardProductCmpt1 = newProductCmpt(productType, "a.StandardProduct1");
        IProductCmpt standardProductCmpt2 = newProductCmpt(productType, "a.StandardProduct2");
        IProductCmpt templatedProductCmpt1 = newProductCmpt(productType, "a.TemplatedProduct1");
        templatedProductCmpt1.setTemplate(template.getQualifiedName());
        templatedProductCmpt1.getIpsSrcFile().save(null);
        IProductCmpt templatedProductCmpt2 = newProductCmpt(productType, "a.TemplatedProduct2");
        templatedProductCmpt2.setTemplate(template.getQualifiedName());
        templatedProductCmpt2.getIpsSrcFile().save(null);

        IIpsElementDecorator decorator = IIpsDecorators.get(standardProductCmpt1.getClass());
        assertNotNull(decorator);
        assertTrue(decorator instanceof ProductCmptDecorator);

        ProductCmptDecorator cmptDecorator = (ProductCmptDecorator)decorator;

        ImageDescriptor standardImageDescriptor = cmptDecorator
                .getImageDescriptor(standardProductCmpt1.getIpsSrcFile());

        assertThat(cmptDecorator.getImageDescriptor(standardProductCmpt2.getIpsSrcFile()),
                is(standardImageDescriptor));

        ImageDescriptor templatedImageDescriptor = cmptDecorator
                .getImageDescriptor(templatedProductCmpt1.getIpsSrcFile());

        assertThat(templatedImageDescriptor, is(not(sameInstance(standardImageDescriptor))));
        assertThat(cmptDecorator.getImageDescriptor(templatedProductCmpt2.getIpsSrcFile()),
                is(sameInstance(templatedImageDescriptor)));
    }

    @Test
    public void testGetImageDescriptor_Null() {
        ProductCmptDecorator decorator = new ProductCmptDecorator();

        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(null);

        assertThat(imageDescriptor, is(decorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor_ElementNull() {
        ProductCmptDecorator decorator = new ProductCmptDecorator();

        ImageDescriptor imageDescriptor = decorator.getImageDescriptor((IIpsElement)null);

        assertThat(imageDescriptor, is(decorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor_ElementTemplate() {
        // create Types
        IProductCmptType productType = newProductCmptType(root, "a.ProductType");
        // create components
        IProductCmpt template = newProductTemplate(productType, "a.Template");
        ProductCmptDecorator decorator = new ProductCmptDecorator();

        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(template);

        assertThat(imageDescriptor, is(descriptorOf(ProductCmptDecorator.PRODUCT_CMPT_TEMPLATE_BASE_IMAGE)));
    }

    @Test
    public void testGetImageDescriptor_SrcFileTemplate() {
        // create Types
        IProductCmptType productType = newProductCmptType(root, "a.ProductType");
        // create components
        IProductCmpt template = newProductTemplate(productType, "a.Template");
        ProductCmptDecorator decorator = new ProductCmptDecorator();

        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(template.getIpsSrcFile());

        assertThat(imageDescriptor, is(descriptorOf(ProductCmptDecorator.PRODUCT_CMPT_TEMPLATE_BASE_IMAGE)));
    }

}
