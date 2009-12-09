/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.IpsUIPluginTest;

public class WorkbenchAdapterTest extends IpsUIPluginTest {

    private IIpsPackageFragmentRoot root;
    private IIpsProject ipsProject;

    private ImageDescriptor prodCmptDefaultIcon;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        prodCmptDefaultIcon = IpsUIPlugin.getDefault().getImageDescriptor("ProductCmpt.gif");
        ipsProject = newIpsProject("AdapterTestProject");
        root = newIpsPackageFragmentRoot(ipsProject, null, "root");
    }

    public void testGetCustomInstanceEnabledIconOrDefault() throws CoreException {
        // create Types
        IProductCmptType aSuperType = newProductCmptType(root, "ASuperType");
        IProductCmptType bNormalType = newProductCmptType(root, "BNormalType");
        IProductCmptType cSubType = newProductCmptType(root, "CSubType");
        bNormalType.setSupertype(aSuperType.getQualifiedName());
        cSubType.setSupertype(bNormalType.getQualifiedName());
        // Define Icon
        bNormalType.setInstancesIcon("root/icons/enabled");
        // create components
        IProductCmpt aSuperCmpt = newProductCmpt(root, "SuperProductCmpt");
        IProductCmpt bNormalCmpt = newProductCmpt(root, "NormalProductCmpt");
        IProductCmpt cSubCmpt = newProductCmpt(root, "SubProductCmpt");

        IWorkbenchAdapter adapter = (IWorkbenchAdapter)aSuperCmpt.getAdapter(IWorkbenchAdapter.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof ProductCmptWorkbenchAdapter);

        ProductCmptWorkbenchAdapter cmptAdapter = (ProductCmptWorkbenchAdapter)adapter;
        cmptAdapter.getImageDescriptorForInstancesOf(aSuperType);

        // A: standard Icons
        ImageDescriptor iconDesc = IpsUIPlugin.getImageDescriptor(aSuperCmpt);
        assertTrue(iconDesc == prodCmptDefaultIcon);
        // B: Custom Icon
        iconDesc = IpsUIPlugin.getImageDescriptor(bNormalCmpt);
        assertEquals(IpsUIPlugin.getDefault().getImageRegistry().getDescriptor("root/icons/enabled"), iconDesc);
        // C inherits B's Custom Icon
        iconDesc = IpsUIPlugin.getImageDescriptor(cSubCmpt);
        assertEquals(IpsUIPlugin.getDefault().getImageRegistry().getDescriptor("root/icons/enabled"), iconDesc);

        cSubType.setInstancesIcon("root/icons/enabledCSubType");
        // C: custom Icon overwrites inherited Icon
        iconDesc = IpsUIPlugin.getImageDescriptor(cSubCmpt);
        assertEquals(IpsUIPlugin.getDefault().getImageRegistry().getDescriptor("root/icons/enabledCSubType"), iconDesc);

        cSubType.setInstancesIcon("");
        // C inherits B's Custom Icons again
        iconDesc = IpsUIPlugin.getImageDescriptor(cSubCmpt);
        assertEquals(IpsUIPlugin.getDefault().getImageRegistry().getDescriptor("root/icons/enabled"), iconDesc);

        bNormalType.setInstancesIcon("");
        // C inherits A's standard Icon
        iconDesc = IpsUIPlugin.getImageDescriptor(cSubCmpt);
        assertEquals(prodCmptDefaultIcon, iconDesc);
    }

}
