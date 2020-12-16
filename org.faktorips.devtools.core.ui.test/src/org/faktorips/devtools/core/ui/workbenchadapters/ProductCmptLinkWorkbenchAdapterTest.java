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
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.junit.Before;
import org.junit.Test;

public class ProductCmptLinkWorkbenchAdapterTest extends AbstractIpsPluginTest {

    private IIpsPackageFragmentRoot root;
    private IIpsProject ipsProject;

    private IProductCmpt prodCmpt;

    private IProductCmptGeneration generation;

    private IProductCmptLink link;

    private ProductCmptLinkWorkbenchAdapter adapter;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("AdapterTestProject");
        root = newIpsPackageFragmentRoot(ipsProject, null, "root");
        adapter = new ProductCmptLinkWorkbenchAdapter();
        IProductCmptType prodType = newProductCmptType(root, "ProdCmptType");
        prodCmpt = newProductCmpt(prodType, "ProdCmpt");
        generation = prodCmpt.getProductCmptGeneration(0);
        link = generation.newLink("ProdCmpt");
        link.setTarget("ProdCmpt");
    }

    @Test
    public void testAdapter() {
        IWorkbenchAdapter adapter = (IWorkbenchAdapter)link.getAdapter(IWorkbenchAdapter.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof ProductCmptLinkWorkbenchAdapter);
    }

    @Test
    public void testProductCmptIconDesc() {
        ImageDescriptor imageDescriptor = adapter.getImageDescriptor(link);

        assertNotNull(imageDescriptor);
        assertTrue(imageDescriptor instanceof DecorationOverlayIcon);
    }

    @Test
    public void testProductCmptIconDescIsReused() {
        ImageDescriptor imageDescriptor1 = adapter.getImageDescriptor(link);
        IProductCmptLink link2 = generation.newLink("ProdCmpt");
        link2.setTarget("ProdCmpt");
        ImageDescriptor imageDescriptor2 = adapter.getImageDescriptor(link2);

        assertThat(imageDescriptor2, is(sameInstance(imageDescriptor1)));
    }
}
