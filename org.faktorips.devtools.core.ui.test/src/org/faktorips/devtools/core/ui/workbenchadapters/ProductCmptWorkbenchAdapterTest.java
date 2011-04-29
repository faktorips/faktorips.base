/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.workbenchadapters.ProductCmptWorkbenchAdapter.DefaultIconDesc;
import org.faktorips.devtools.core.ui.workbenchadapters.ProductCmptWorkbenchAdapter.IconDesc;
import org.faktorips.devtools.core.ui.workbenchadapters.ProductCmptWorkbenchAdapter.PathIconDesc;
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

        IWorkbenchAdapter adapter = (IWorkbenchAdapter)aSuperCmpt.getAdapter(IWorkbenchAdapter.class);
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

}
