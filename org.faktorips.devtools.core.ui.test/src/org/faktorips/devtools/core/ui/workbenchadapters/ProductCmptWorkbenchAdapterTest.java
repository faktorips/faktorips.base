/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.workbenchadapters.ProductCmptWorkbenchAdapter.DefaultIconDesc;
import org.faktorips.devtools.core.ui.workbenchadapters.ProductCmptWorkbenchAdapter.IconDesc;
import org.faktorips.devtools.core.ui.workbenchadapters.ProductCmptWorkbenchAdapter.PathIconDesc;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.junit.Before;
import org.junit.Test;

// TODO Joerg warum musste vom IpsUIPluginTest abgeleitet werden
// Problem beim der Autotestsuite (stefan w.)?
// die Tests von IpsUIPluginTest wurden immer mit ausgefuehrt
public class ProductCmptWorkbenchAdapterTest extends AbstractIpsPluginTest {

    private IIpsPackageFragmentRoot root;
    private IIpsProject ipsProject;

    private ImageDescriptor prodCmptDefaultIcon;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        prodCmptDefaultIcon = IpsUIPlugin.getImageHandling().createImageDescriptor("ProductCmpt.gif");
        ipsProject = newIpsProject("AdapterTestProject");
        root = newIpsPackageFragmentRoot(ipsProject, null, "root");
    }

    @Test
    public void testProductCmptIconDesc() throws CoreException {
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

        IWorkbenchAdapter adapter = aSuperCmpt.getAdapter(IWorkbenchAdapter.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof ProductCmptWorkbenchAdapter);

        ProductCmptWorkbenchAdapter cmptAdapter = (ProductCmptWorkbenchAdapter)adapter;

        // A: standard Icons
        ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().getImageDescriptor(aSuperCmpt);
        assertEquals(prodCmptDefaultIcon, imageDescriptor);
        IconDesc iconDesc = cmptAdapter.getProductCmptIconDesc(aSuperType);
        assertTrue(iconDesc instanceof DefaultIconDesc);
        // B: Custom Icon
        iconDesc = cmptAdapter.getProductCmptIconDesc(bNormalType);
        assertTrue(iconDesc instanceof PathIconDesc);
        assertEquals("normalTypeImage.gif", ((PathIconDesc)iconDesc).getPathToImage());
        // C inherits B's Custom Icon
        iconDesc = cmptAdapter.getProductCmptIconDesc(cSubType);
        assertTrue(iconDesc instanceof PathIconDesc);
        assertEquals("normalTypeImage.gif", ((PathIconDesc)iconDesc).getPathToImage());

        cSubType.setInstancesIcon("subTypeImage.gif");
        // C: custom Icon overwrites inherited Icon
        iconDesc = cmptAdapter.getProductCmptIconDesc(cSubType);
        assertTrue(iconDesc instanceof PathIconDesc);
        assertEquals("subTypeImage.gif", ((PathIconDesc)iconDesc).getPathToImage());

        cSubType.setInstancesIcon("");
        // C inherits B's Custom Icons again
        iconDesc = cmptAdapter.getProductCmptIconDesc(cSubType);
        assertTrue(iconDesc instanceof PathIconDesc);
        assertEquals("normalTypeImage.gif", ((PathIconDesc)iconDesc).getPathToImage());

        bNormalType.setInstancesIcon("");
        // C inherits A's standard Icon
        imageDescriptor = IpsUIPlugin.getImageHandling().getImageDescriptor(cSubCmpt);
        assertEquals(prodCmptDefaultIcon, imageDescriptor);
        iconDesc = cmptAdapter.getProductCmptIconDesc(cSubType);
        assertTrue(iconDesc instanceof DefaultIconDesc);
    }

    @Test
    public void testIconDescriptorsForTemplatesAreReused() throws CoreException {
        // create Types
        IProductCmptType productType = newProductCmptType(root, "a.ProductType");
        // create components
        ProductCmpt template = newProductTemplate(productType, "a.Template");
        IProductCmpt standardProductCmpt1 = newProductCmpt(productType, "a.StandardProduct1");
        IProductCmpt standardProductCmpt2 = newProductCmpt(productType, "a.StandardProduct2");
        IProductCmpt templatedProductCmpt1 = newProductCmpt(productType, "a.TemplatedProduct1");
        templatedProductCmpt1.setTemplate(template.getQualifiedName());
        templatedProductCmpt1.getIpsSrcFile().save(true, null);
        IProductCmpt templatedProductCmpt2 = newProductCmpt(productType, "a.TemplatedProduct2");
        templatedProductCmpt2.setTemplate(template.getQualifiedName());
        templatedProductCmpt2.getIpsSrcFile().save(true, null);

        IWorkbenchAdapter adapter = standardProductCmpt1.getAdapter(IWorkbenchAdapter.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof ProductCmptWorkbenchAdapter);

        ProductCmptWorkbenchAdapter cmptAdapter = (ProductCmptWorkbenchAdapter)adapter;

        ImageDescriptor standardImageDescriptor = cmptAdapter.getImageDescriptor(standardProductCmpt1.getIpsSrcFile());
        assertThat(cmptAdapter.getImageDescriptor(standardProductCmpt2.getIpsSrcFile()),
                is(sameInstance(standardImageDescriptor)));

        ImageDescriptor templatedImageDescriptor = cmptAdapter
                .getImageDescriptor(templatedProductCmpt1.getIpsSrcFile());
        assertThat(templatedImageDescriptor, is(not(sameInstance(standardImageDescriptor))));
        assertThat(cmptAdapter.getImageDescriptor(templatedProductCmpt2.getIpsSrcFile()),
                is(sameInstance(templatedImageDescriptor)));
    }

}
